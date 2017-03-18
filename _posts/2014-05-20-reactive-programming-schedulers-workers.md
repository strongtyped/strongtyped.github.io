---
layout: post
title: "Schedulers and Workers"
date: 2014-05-20 09:52:00 +0200
comments: true
categories:
  - scala
  - reactive
image: https://source.unsplash.com/OiiThC8Wf68/2000x1000?a=.png
author_staff_member: luc
---


This is a third blog post about _Reactive Programming in Scala_.
The first two blog posts,
[_Reactive Programming: Try versus Future_](/blog/2014/05/11/reactive-programming-try-future)
resp.
[_Reactive Programming: Notification versus Observable_](/blog/2014/05/14/reactive-programming-notification-observable),
covered reactive programs resulting in _one value_ resp. _many values_.
This blog post covers _schedulers_ and _workers_.
They are used to transform _iterables_ to _observables_.
As mentioned in
[_Reactive Programming: Notification versus Observable_](/blog/2014/05/14/reactive-programming-notification-observable),
the transformation from iterables to observables is not as trivial as it looks: at any moment subscribed observers should be able to unsubscribe.

__Remark:__ 
The types that are used in this blog post exist in the
[Scala Adaptor for RxJava](https://github.com/Netflix/RxJava/tree/master/language-adaptors/rxjava-scala) library.
We refer to this library as the RxScala library.
In contrast with the previous two blog posts, this blog post deals with only one function: `iterable2observable`.
Moreover, the `Observable` companion object of the RxScala library already has a `from` method, having an iterable parameter,
that is equivalent to `iterable2observable`.
Dedicating a separate blog post on the definition of `iterable2observable` is done for didactical reasons only.

<!-- more -->

## A first attempt: the `iterable2observable01` function


In the
[_Reactive Programming: Notification versus Observable_](/blog/2014/05/14/reactive-programming-notification-observable) blog post
we defined a function `mkObservable` with the following signature

* `def mkObservable[Z](sz2u: Subscriber[Z] => Unit): Observable[Z]`

The function `mkObservable` can be used to define an observable by defining what it pushes to subscribed observers.
Using `mkObservable` we can naturally define a function `iterable2observable01`

```scala
  def iterable2observable01[Z](iz: Iterable[Z]): Observable[Z] =
    mkObservable[Z] { sz =>
      iz.foreach { z => sz.onNext(z) } 
      sz.onCompleted()
    }
```

describe a computation `observable01` using `iterable2observable01`,
and use the computation `observable01` as follows

```scala
  def main(args: Array[String]) {
     lazy val observable01 = iterable2observable01((1 to 10).toList)
     val subscription01 = subscribe(observable01)
     subscription01.unsubscribe() 
     println() 
  }
```

resulting in `12345678910completed` being printed.

It looks as if `unsubscribe` does not really work.

## A second attempt: the `iterable2observable02` function

Maybe we should run the code 

* `iz.foreach { z => sz.onNext(z) } ; sz.onCompleted()`

in a separate thread, so that we can unscubscribe from the main thread.

Using _schedulers_, and _workers_ that can be created using them,
we can naturally define a function `iterable2observable02`
that schedules the code above to run after a _delay_ of duration `delay`

```scala
  def iterable2observable02[Z]
   (iz: Iterable[Z], delay: Duration)
   (implicit scheduler: Scheduler): Observable[Z] = {
     val iterator = iz.iterator
     mkObservable[Z] { sz =>
       scheduler.createWorker.schedule({ _ => 
         iz.foreach { z => sz.onNext(z) } 
          sz.onCompleted()
       }, delay)
     }
  }
```

describe a computation `observable02` using `iterable2observable02`,
and use the computation `observable02` as follows

```scala
  def main(args: Array[String]) {
     implicit val scheduler = NewThreadScheduler()
     lazy val observable02 = iterable2observable02((1 to 10).toList, 1 second)
     val subscription02 = subscribe(observable02)
     subscription02.unsubscribe() 
     Thread.sleep(990)
     println() 
  } 
```

resulting in nothing being printed at all,

or as follows

```scala
  def main(args: Array[String]) {
     implicit val scheduler = NewThreadScheduler()
     lazy val observable02 = iterable2observable02((1 to 10).toList, 1 second)
     val subscription02 = subscribe(observable02)
     subscription02.unsubscribe() 
     Thread.sleep(1010)
     println() 
  } 
```

resulting in `12345678910completed` being printed.

Ok, we have some period of time during which we can unsubscribe,
but, once the observable starts pushing we cannot unsubscribe any more.

It looks as if `unsubscribe` still does not work.

## A final attempt: the `iterable2observable` function

The _first_ idea behind the `iterable2observable` function is to replace the iterative code 

* `iz.foreach { z => sz.onNext(z) } ; sz.onCompleted()`

where `iz` is an _iterable_, or, equivalently, the iterative code

* `while(iz.hasNext) { sz.onNext(iz.next()) } ; sz.onCompleted()`

where `iz` is an _iterator_, by code

* `if(iz.hasNext) sz.onNext(iz.next()) else sz.onCompleted()`

where `iz` is an iterator, and, somehow, do the iteration steps ourselves.

The _second_ idea behind the `iterable2observable` function is to not only
have a _delay_ of a certain duration _before the first iteration step_, but to also
have a _period_ of a certain duration _between consecutive iteration steps_.

The _third_ idea behind the `iterable2observable` function is to work with a
_multi assignment subscription_ that is updated during every iteration step.


The `iterativeSchedule` method below uses those three ideas

```scala
  implicit class WorkerOps(worker: Worker) {
    def iterativeSchedule
      (delay: Duration, period: Duration)
      (action: Unit => Unit): Subscription = {
      val multipleAssignmentSubscription = MultipleAssignmentSubscription();
      lazy val recursiveAction: Unit => Unit = _ => {
      var count: Long = 0
        if(!multipleAssignmentSubscription.isUnsubscribed) {
           action(())
           count = count + 1
           multipleAssignmentSubscription.subscription =
             worker.schedule(recursiveAction, delay + count * period)
        }
      }
      multipleAssignmentSubscription.subscription =
        worker.schedule(recursiveAction, delay)
      multipleAssignmentSubscription
    }
  }
```

We used a `var count` as a counter. You may argue: how ugly!
Well, in a way, using a `var` as an _internal implementation detail_ is not ugly at all.
Erik Meijer (the brain behind the Rx extensions of .NET, upon which RxJava
and RxScala are based) calls himself _"both a functional thinker and an imperative hacker"_.


We can now naturally define a function `iterable2observable`
that schedules the code 

* `if(iz.hasNext) sz.onNext(iz.next()) else sz.onCompleted()`

to run for the first time after a delay of a duration `delay`,
and to also continue running after delays of duration `delay + count * period`

```scala
  private def iterator2observable[Z]
    (iz: Iterator[Z], delay: Duration, period: Duration)
    (implicit scheduler: Scheduler): Observable[Z] =
      mkObservable[Z] { sz =>
        scheduler.createWorker.iterativeSchedule(delay, period) { _ =>
          if(iz.hasNext) sz.onNext(iz.next())
          else sz.onCompleted()
        }
      }

  def iterable2observable[Z]
    (iz: Iterable[Z], delay: Duration, period: Duration)
    (implicit scheduler: Scheduler): Observable[Z] =
    iterator2observable(iz.iterator, delay, period)
```

We can now describe a computation `observable` using `iterable2observable`,
and use the computation `observable` as follows

```scala
  def main(args: Array[String]) {
     implicit val scheduler = NewThreadScheduler()
     lazy val observable = iterable2observable((1 to 10).toList, 0 second, 1 second)
     val subscription = subscribe(observable)
     Thread.sleep(4500)
     subscription.unsubscribe()
     println()
  }
```

resulting in `12345` being printed.


## Infinite Observables

The code below also works with _infinite iterables_ like the `nats` iterable below

```scala
  def nats: Iterable[Int] = new Iterable[Int] {
    var i = 0
    def iterator: Iterator[Int] = new Iterator[Int] {
      def hasNext: Boolean = true
      def next(): Int = { 
        i = i + 1
        i
      }
    }
  }
```

We can now describe a computation `natsObservable` using `iterable2observable`
and use the computation `natsObservable` as follows

```scala
  def main(args: Array[String]) {
     implicit val scheduler = NewThreadScheduler()
     lazy val natsObservable = iterable2observable(nats, 0 second, 1 second)
     val natsSubscription = subscribe(natsObservable)
     Thread.sleep(4500)
     natsSubscription.unsubscribe()
     println()
  }
```

resulting in `12345` being printed



















