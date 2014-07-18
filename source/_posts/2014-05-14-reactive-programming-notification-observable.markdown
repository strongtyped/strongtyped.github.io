---
layout: post
title: "Reactive Programming: Notification versus Observable"
date: 2014-05-14 08:25:00 +0200
author: Luc Duponcheel
comments: true
---

This is a second of two blog posts about _Reactive Programming in Scala_.
The first blog post about [_Reactive Programming: Try versus Future_](/blog/2014/05/11/reactive-programming-try-future)
covered _reactive programs resulting in one value_.
This second blog post covers _reactive programs resulting in many (zero or more) values_.

__Remark:__ 
The types that are used in this blog post exist in the
[Scala Adaptor for RxJava](https://github.com/Netflix/RxJava/tree/master/language-adaptors/rxjava-scala) library.
We refer to this library as the RxScala library.
In contrast with the previous blog post, this blog post puts less emphasis on how
to _define_ computations. It puts more emphasis on how to _use_ computations.

<!-- more -->

The `Notification` type
-----------------------

The `Notification` type plays a role similar to the `Try` type, but
it is not considered as a _computation_ type (although it could be).
Just like `Try` is a type that is similar to `Option`,
`Notification` is a type that is similar to `Iterable`
(but they both also somehow deal with failure).

The `Notification` trait represents a value that is
a _next_ value, an _error value_ or a _marker value_
indicating that there are no more values.
An instance of `Notification[Z]` is
an instance of `OnNext[Z]` holding a value of type `Z`,
an instance of `OnError[Z]` holding an error of type `Throwable`
or an instance of `OnCompleted[Z]` holding no value at all.

Code that makes use a value `nz` of type `Notification[Z]`,
typically, makes use of a `match` on `nz`, which, you can think of as code that

* in a synchronous (blocking) way, _pulls_ a
next value, error value or marker non-value
out of `nz`.

In fact, this blog post is rarely uses notifications directly in this way.
Much in the spirit of reactive programming,
and as the names of the classes extending `Notification` suggest,
notifications values are at their best when used as values that
are _pushed_ rather than _pulled_.

Notifications values can, just as computations of type `Try`, be materialized.
This blog post does not go into details.

Computations of type `Observable`
---------------------------------

A value of type `Notification[Z]` that is defined as a `def`
is essentially the same as a value of type `Unit => Notification[Z]`.
The _dual_ of such a value, obtained by reversing the arrow `=>`,
is a value of type `Notification[Z] => Unit`.
This dual is, in a way, the same as a value of type `Observer[Z]`
since the `Observer[-Z]` trait of RxScala has three methods

* `def onNext(value: Z): Unit`
* `def onError(error: Throwable): Unit`
* `def onCompleted(): Unit`

In a way (as illustrated in the following section)
an `Observable[Z]` value is the _dual_ of an `Observer[Z]` value,
and, as such, the _bi-dual_ of a `Notification[Z]` value.

More precisely, in a way, `Observable[Z]` is the same as `Observer[Z] => Unit`,
which, in a way, is the same as `(Notification[Z] => Unit) => Unit`.

This _bi-duality_ turns out to transform synchronous values of type `Notification`
into asynchronous computations of type `Observable`.

Materializing failure and latency using `mkObservable`
------------------------------------------------------

Just as the code in the [previous blog post](/blog/2014/05/11/reactive-programming-try-future) did for `Future`
the code below materializes failure and latency for `Observable`

``` scala
  def mkObservable[Z](sz2u: Subscriber[Z] => Unit): Observable[Z] =
    Observable(sz2u)
```

The `Observable` companion object of the RxScala library has an `apply` method 
that we use (in a trivial way) to define `mkObservable`.
The definition of `mkObservable` is given for consistency reasons only
(cfr. `mkTry` and `mkFuture` of the [ previous blog post](/blog/2014/05/11/reactive-programming-try-future)).

We did not define `Subscriber[Z]` yet. Below is its definition

* `trait Subscriber[-Z] extends Observer[Z] with Subscription`

So, a subscriber is just a special observer.

When considering `mkObservable` as the _only_ way to
make an observable, in a way, `Observable[Z]` becomes the same as `Observer[Z] => Unit`.

An observer can _subscribe_ to an observable as shown by the following method of the `Observable[Z]` class below

* `def subscribe(observer: Observer[T]): Subscription`

We did not define `Subscription` yet. It is a trait with the following methods

* `def isUnsubscribed: Boolean`
* `def unsubscribe(): Unit`

The main reason why _subscriptions_ exist is because observables
push _many_ values to subscribed observers and there should be a way for subscribed observers to
unsubscribe from a subscription so that no more values are pushed to them.

It is important that you understand the _usage_ of `mkObservable`

* you can make an observable by defining what it _pushes_ to subscribed observers

Not surprising, a computation of type `Future` can be transformed to a computation
of type `Observable` using this idea

```scala
  def future2observable[Z](fz: => Future[Z]): Observable[Z] =
    Observable { sz =>
      fz.register {
        case Success(z) =>
          sz.onNext(z)
          sz.onCompleted()
        case Failure(t) =>
          sz.onError(t)
      }
    }
```

The `future2observable` code makes use of a call by name parameter `fz`.

The example below illustrates how to use `future2observable` to transform a computation
of type `Try` to a computation of type `Observable`
 
``` scala
  def try2observable[Z](tz: => Try[Z]): Observable[Z] =
    future2observable(try2future(tz))
```

The `try2future` function for computations of type `Try` could naturally be reused.

The example below illustrates how to use `try2observable`

``` scala
  def mkObservableTry[Z](block: => Z): Observable[Z] =
    try2observable(mkTry{ block }) 
```

The `mkTry` function for computations of type `Try` could naturally be reused.

The `mkObservableTry` code makes use of a _call by name_ parameter `block`
which you can think of as code that 
on the one hand, _can succeed_ or _can fail_ and,
on the other hand, _can take some time to be evaluated_.

There is one concept that we did not explain yet.
In the [previous blog post](/blog/2014/05/11/reactive-programming-try-future)
the type `Promise[Z]` is used in the implementation of many methods.
We also pounted out that it is a useful type on its own.
There exists a corresponding concept that is formalized as

* `trait Subject[-Z] extends Observer[-Z] with Observable[+Z]`

It is possible, and often useful, to define observables directly in terms of
_various kinds of_ subjects (`Subject` is a trait).
This blog post does not go into details.

How are computations of type `Future` described?

Describing Computations of type `Observable`
--------------------------------------------

Here are four computations of type `Observable` that are described using `future2observable`

``` scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

lazy val tryObservableFooBar05: Observable[String] =
  future2observable(tryFutureFooBar05)

lazy val tryObservableFooBar06: Observable[String] =
  future2observable(tryFutureFooBar06)

lazy val tryObservableFooBar07: Observable[String] =
  future2observable(tryFutureFooBar07)

lazy val tryObservableFooBar08: Observable[String] =
  future2observable(tryFutureFooBar08)


// Exiting paste mode, now interpreting.

tryObservableFooBar05: rx.lang.scala.Observable[String] = <lazy>
tryObservableFooBar06: rx.lang.scala.Observable[String] = <lazy>
tryObservableFooBar07: rx.lang.scala.Observable[String] = <lazy>
tryObservableFooBar08: rx.lang.scala.Observable[String] = <lazy>

```

The examples make use of computations of type `Future`
from [the previous blog post](/blog/2014/05/11/reactive-programming-try-future) 

How are computations of type `Observable` _executed_ once they are described?

Executing Computations of type `Observable`
-------------------------------------------

For the purpose of this series of blog posts,
executing a computation of type `Observable` can be as simple as
subscribing an observer that prints the values pushed to it, or
prints the message of an error pushed to it, or
prints an indication that no more more values are pushed.

``` scala
  def subscribe[Z](oz: Observable[Z]): Subscription =
    oz.subscribe(new Observer[Z] {
    override def onNext(z: Z) = print(s"$z")
    override def onError(t: Throwable) = print(s"${t.getMessage}")
    override def onCompleted() = print("completed")
  })
```

We have called the method `subscribe` instead of `execute`
because it corresponds to a `subscribe` method of `Observable` that,
in this case , also returns a subscription that can be unsubscribed.

Below, the four computations of type `Observable` are executed using `subscribe`

``` scala
scala> { subscribe(tryObservableFooBar05) ; work('W', 150) }
B54.F194.W192.B54.F194.B54.B54.W192.F194.B54.B54.F194.B54.W192.B54.F194.B54.B54.W192.
F194.F194.W192.F194.W192.F194.F194.W192.fooOkbarOkcompletedW192.W192.W192.
scala> { subscribe(tryObservableFooBar06) ; work('W', 150) }
B54.F196.W192.B54.B54.F196.B54.W192.F196.B54.B54.F196.W192.B54.B54.F196.B54.W192.B54.
F196.W192.F196.F196.W192.F196.W192.F196.fooKoW192.W192.W192.
scala> { subscribe(tryObservableFooBar07) ; work('W', 150) }
B196.F54.W192.B196.F54.B196.W192.B196.F54.B196.B196.W192.F54.B196.B196.F54.B196.W192.
B196.F54.W192.F54.F54.W192.F54.W192.F54.barKoW192.W192.W192.
scala> { subscribe(tryObservableFooBar08) ; work('W', 150) }
B54.F197.W192.B54.F197.B54.W192.B54.F197.B54.B54.W192.F197.B54.B54.F197.B54.W192.B54.
F197.W192.F197.F197.W192.F197.W192.F197.barKo and fooKoW192.W192.W192.
```

Transforming futures, pushing one value,
to (a special  kind of) observables, potentially pushing many (zero or more) values,
is, of course, not the end of the story about observables.

More Computations of type `Observable`
--------------------------------------

The simple observables defined below can serve as building blocks for defining more complex observables

``` scala
  def zeroObservable(): Observable[Nothing] =
    mkObservable { sn =>
    }


  def errorObservable[Z](t: Throwable): Observable[Z] =
    Observable { sz =>
      sz.onError(t)
    }

  def nextObservable[Z](z: Z): Observable[Z] =
    Observable { sz =>
      sz.onNext(z)
    }

  def completedObservable(): Observable[Nothing] =
    Observable { sn =>
      sn.onCompleted()
    }

  def oneObservable[Z](z: Z): Observable[Z] =
    Observable { sz =>
      sz.onNext(z)
      sz.onCompleted()
    }
```

The meaning of the simple observables above is almost trivial:
they push _nothing_ resp. an _error_ resp. a _value_, resp. a _completed marker_ resp. a _value and a completed marker_ to subscribed observers.

The following method shows how to define observables that push many values.
It uses a helper function `subscriber2observable` because directly subscribing
the subscriber `sz` below to the observable `oz` below does not work:
a new observer, wrapping the subscriber, needs to be subscribed.

``` scala
  def subscriber2observer[Z](sz: Subscriber[Z]) =
    new Observer[Z] {
    override def onNext(z: Z) = sz.onNext(z)
    override def onError(t: Throwable) = sz.onError(t)
    override def onCompleted() = sz.onCompleted()
  }

  implicit class ObservableOps[Z](oz: Observable[Z]) {
    def startingWith(zs: Z*) =
      Observable[Z] { sz =>
        zs.foreach { z => sz.onNext(z) }
        oz.subscribe(subscriber2observer(sz))
      }
    // ...
```

First all `zs` are pushed to subscribed observers
and after that the observable `oz` continues pushing
values to them.

``` scala
scala> subscribe(oneObservable(4).startingWith(1, 2, 3))
1234completed
``` 

Computations of type `Observable` of the RxScala library
--------------------------------------------------------

There are various ways to define observables available in the RxScala library.

Below is an example, using `from`, that
can be used to transform any _iterable_, which is pull based,
to an _observable_, which is push based

``` scala
scala> lazy val oneTwoThreeFour = from(List(1, 2, 3, 4))
oneTwoThreeFour: rx.lang.scala.Observable[Int] = <lazy>
```

You can subscribe to it (ignoring the subscription)

``` scala
scala> { subscribe(oneTwoThreeFour) ; () }
1234completed
```

This transformation from iterables to observables is not as trivial
as it looks: at any moment subscribed observers should be able to
unsubscribe.

Below is an example, using `interval`, that defines
a _ticker_  that is ticking every `100` milliseconds.

``` scala
scala> lazy val ticker = interval(100 millis)
ticker: rx.lang.scala.Observable[Long] = <lazy>
```
You can subscribe to it and unsubscribe from it when
you are not interested in receiving further ticks any more.

``` scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

{
 val subscription = subscribe(ticker)
 Thread.sleep(600)
 subscription.unsubscribe
}


// Exiting paste mode, now interpreting.

012345
```

So what about `bnd` and `and`?

`flatten` is your friend
------------------------

The story is somewhat different here and we do not go into all details.
For observables, the method `bnd` below which is defined in terms of the RxScala
library method `flatten`
(which, _non-deterministically_, _merges observables of observables_)
is all we need. 

``` scala
    def bnd[Y](z2oy: Z => Observable[Y]): Observable[Y] =
      Observable[Observable[Y]] { ooy =>
        oz.subscribe(new Observer[Z] {
          override def onNext(z: Z) =
            ooy.onNext(z bind z2oy)
          override def onError(t: Throwable) =
            ooy.onError(t)
          override def onCompleted() =
            ooy.onCompleted()
        })
      } flatten
```

To make things interesting from a merge point of view we define
the following verbose version of `interval`

``` scala
  def verboseInterval(t0: Long, x: Long, n: Int) =
    interval(x*100 millis).take(n) bnd { y => 
      val millis = (System.currentTimeMillis - t0) / 1000.0
      println(f"at ${millis}%.3f interval($x) emits $y")
      nextObservable(y)
  }
```

Here is an observable, this time defined as a `def`,
to take the current time into account every time it is executed,
that uses `bnd` to merge three intervals as they are pushed
to subscribed observers

```
scala> :paste
// Entering paste mode (ctrl-D to finish)

def mergedIntervals(t: Long) =
    from(List(3, 2, 1)) bnd { x =>
      verboseInterval(t, x, 2) bnd { y =>
        oneObservable("")
      }
    }

// Exiting paste mode, now interpreting.

mergedIntervals: (t: Long)rx.lang.scala.Observable[String]
```

It is instructive to try to understand the output of executing the following computation

``` scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

{
 subscribe(mergedIntervals(System.currentTimeMillis))
 Thread.sleep(3000)
}

// Exiting paste mode, now interpreting.

at 0.102:interval(1) emits 0
at 0.202:interval(2) emits 0
at 0.202:interval(1) emits 1
at 0.302:interval(3) emits 0
at 0.402:interval(2) emits 1
at 0.602:interval(3) emits 1

```

An example: `scanning`
----------------------

We end with a common use case: _scanning observables_.
For example:
an observable that pushes `"1" "2" "3" "4" "5"`,
when scanned using `add`, a swapped, curried version of `+`, becomes
an observable that pushes `"1" "12" "123" "1234" "12345"`. 

``` scala
     def scanning[Y](y: Y)(z_2_y2y: Z => Y => Y): Observable[Y] =
      mkObservable[Y] { sz =>
        var accY = y
        oz.subscribe(new Observer[Z] {
          override def onNext(z: Z) = {
            accY = z_2_y2y(z)(accY) ; sz.onNext(accY)
          }
          override def onError(t: Throwable) = {
            sz.onError(t)
          }
          override def onCompleted() = {
            sz.onCompleted()
          }
        })
      }
```

We used a `var accY` as an accumulator. You may argue: how ugly!
Well, in a way, using a `var` as an _internal implementation detail_ is not ugly at all.
Erik Meijer (the brain behind the Rx extensions of .NET, upon which RxJava
and RxScala are based) calls himself _"both a functional thinker and an imperative hacker"_.

Here is a typical `scanning` example

``` scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

def add(s: String)(t: String): String = t + s

lazy val scannedObservable =
 from(List("1", "2", "3", "4", "5"))
  .scanning("")(add)

// Exiting paste mode, now interpreting.

add: (s: String)(t: String)String
scannedObservable: rx.lang.scala.Observable[String] = <lazy>
```

and here we run it

``` scala
scala> { subscribe(scannedObservable) ; () }
112123123412345completed
```

