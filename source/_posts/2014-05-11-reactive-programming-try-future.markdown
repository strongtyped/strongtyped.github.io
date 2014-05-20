---
layout: post
title: "Reactive Programming: Try versus Future"
date: 2014-05-11 06:21:00 +0200
author: Luc Duponcheel
comments: true
---

This is a first of two blog posts about _Reactive Programming in Scala_.
The blog post about [_Functional Programming in Scala_](/blog/2014/05/11/functional-programming-in-scala)
introduced _pure computations resulting in exactly one value_.
Reactive programming deals, in a functional way,
with two fundamental _impure_ aspects of computations: _failure_ and _latency_.
The first blog post covers reactive programs resulting in _one_ value.
The second blog post covers Reactive Programs resulting in _many_ (_zero or more_) values.

__Warning:__ 
The types that are used in this blog post exist in the standard Scala library.
For didactical reasons this blog post sometimes defines extra types and extra methods for existing types.

<!-- more -->

Failure
-------

In Java (and many other languages for that matter) failure is modeled using _exceptions_.
Java also added the distiction between _checked_ and _unchecked_ exceptions.

When asking Java programmers if they _like_ checked exceptions, many of them say they _hate_ them.
In a way this is strange because checked exceptions are part of the _contract_ of your code,
and, when using them, the compiler _helps_ you by telling you whether or not
your code _satisfies that contract_.
In practice, many Java programmers have the feeling that the compiler is _annoying_
them rather then helping them.

Why?

Well, because, when trying to satisfy the contract that comes with checked exceptions, 
programmers are basically _left on their own_. As a consequence `try/catch` code propagates
all the way through their code, cluttering up the _essential part_ of their code. 
In a way, when using checked exceptions, _they end up in_ `try/catch` _hell_.
Therefore many Java programmers `catch` checked exceptions and `throw` unchecked ones.

That makes the situation even worse since, by throwing unchecked exceptions,
programmers let failure _silently disappear at compile time_
until it _loudly re-appears at runtime_.

There must be a better way to deal with failure.

Computations of type `Try`
--------------------------

The `Try` type represents a computation that may
either result in a successfully computed value 
or result in a failure.
An instance of `Try[Z]` is
either an instance of `Success[Z]`
or an instance of `Failure[Z]`.

Code that makes use a value `tz` of type `Try[Z]`,
typically, makes use of a `match` on `tz`, which, you can think of as code that

* in a synchronous (blocking) way, _pulls_ a value or a throwable out of `tz`

Materializing failure using `mkTry`
-----------------------------------

A first step towards dealing with failure in a better way is to write, once and for all, code that
_materializes failure at compile time_

``` scala
  def mkTry[Z](block: => Z): Try[Z] =
    try {
      Success { block }
    } catch {
      case throwable: Throwable =>
        Failure(throwable)
    }
```

The `mkTry` code makes use of a _call by name_ parameter `block`
which you can think of as code that _can succeed_ or _can fail_.

The `Try` companion object of the standard Scala library already has an `apply` method
that is equivalent to `mkTry`. The definition of `mkTry` is given for didactical reasons only.

How are computations of type `Try` _described_?

Describing Computations of type `Try`
-------------------------------------

To make things interesting from a failure and latency
point of view, many example computations make use of
the functions `work` and `block` below

``` scala
  def work(c: Char, l: Long) {
    (1 to 10).foreach { _ =>
      print(s"${c}${Thread.currentThread().getId()}.")
      Thread.sleep(l)
    }
  }

  def block(string: String, b: Boolean, c: Char, l: Long): String = {
    work(c, l)
    if(b) s"${string}Ok"
    else throw new Throwable(s"${string}Ko")
  }
```

Here are two computations of type `Try` that are described using `mkTry`

``` scala
scala> lazy val okTryFoo = mkTry { block("foo", true, 'F', 100) }
okTryFoo: scala.util.Try[String] = <lazy>

scala> lazy val koTryFoo = mkTry { block("foo", false, 'F', 100) }
okTryFoo: scala.util.Try[String] = <lazy>
```

A `lazy val` is used to separate the description of the computation from its execution.

How are computations of type `Try` _executed_ once they are described?

Executing Computations of type `Try`
------------------------------------

For the purpose of this series of blog posts,
executing a computation of type `Try` can be as simple as either printing the value
pulled out of a successful result or printing the message pulled out of an unsuccessful result

``` scala
  def execute[Z](tz: Try[Z]): Unit =
    tz match {
      case Success(z) => print(s"$z")
      case Failure(t) => print(s"${t.getMessage}")
    }
```

Below, the two computations of type `Try` are executed using `execute`

``` scala
scala> { execute(okTryFoo) ; work('M', 100) }
F142.F142.F142.F142.F142.F142.F142.F142.F142.F142.fooOkM142.M142.M142.M142.M142.M142.
M142.M142.M142.M142.
scala> { execute(okTryFoo) ; work('M', 100) }
fooOkM142.M142.M142.M142.M142.M142.M142.M142.M142.M142.
scala> { execute(koTryFoo) ; work('M', 100) }
F142.F142.F142.F142.F142.F142.F142.F142.F142.F142.fooKoM142.M142.M142.M142.M142.M142.
M142.M142.M142.M142.
scala> { execute(koTryFoo) ; work('M', 100) }
fooKoM142.M142.M142.M142.M142.M142.M142.M142.M142.M142.
```

So, what does materialized failure bring you?

`bnd` is your guide
-------------------

Just as it is possible for computations of type `One` it is possible for computations of type `Try`
to define a method `bnd` that can be used to _compose computations_

``` scala
  implicit class TryOps[Z](tz: Try[Z]) {
    def bnd[Y](z2ty: Z => Try[Y]): Try[Y] =
      tz match {
        case Success(z) =>
          z bind z2ty
        case Failure(t) =>
          Failure(t)
      }
    // ...
```

An important requirement for `z2ty` is that it _must not throw exceptions_.
Using `mkTry`, it is easy to fulfill this requirement.

With `bnd` in your toolbox, you are not on your own any more to deal with materialized failure.
Indeed, the method `bnd` is your trustworthy guide that

* _leads you through the successful execution path_
* _does the failure related boilerplate work for you_


DSL for declaring computations of type `Try` (Part 1)
-----------------------------------------------------

By now we have defined `mkTry` and `bnd`

* They constitute _a declarative DSL for describing computations_ of type `Try`

We have also defined `execute`

* It constitutes _an imperative DSL for executing computations_ of type `Try`

This section contains some examples on how to make use of those DSL's.

``` scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

lazy val tryFooBar01: Try[String] =
  mkTry { block("foo", true, 'F', 100) } bnd { s =>
    mkTry { block("bar", true, 'B', 100) } bnd { t =>
      mkTry(add(s)(t))
    }
  }

lazy val tryFooBar02: Try[String] =
  mkTry { block("foo", true, 'F', 100) } bnd { s =>
    mkTry { block("bar", false, 'B', 100) } bnd { t =>
      mkTry(add(s)(t))
    }
  }

lazy val tryFooBar03: Try[String] =
  mkTry { block("foo", false, 'F', 100) } bnd { s =>
    mkTry { block("bar", true, 'B', 100) } bnd { t =>
      mkTry(add(s)(t))
    }
  }

lazy val tryFooBar04: Try[String] =
  mkTry { block("foo", false, 'F', 100) } bnd { s =>
    mkTry { block("bar", false, 'B', 100) } bnd { t =>
      mkTry(add(s)(t))
    }
  }

// Exiting paste mode, now interpreting.

tryFooBar01: scala.util.Try[String] = <lazy>
tryFooBar02: scala.util.Try[String] = <lazy>
tryFooBar03: scala.util.Try[String] = <lazy>
tryFooBar04: scala.util.Try[String] = <lazy>

```

We can now, so to speak, try to print `fooOkbarOk` in a synchronous way

``` scala
scala> { execute(tryFooBar01) ; work('M', 100) }
F142.F142.F142.F142.F142.F142.F142.F142.F142.F142.B142.B142.B142.B142.B142.B142.B142.
B142.B142.B142.fooOkbarOkM142.M142.M142.M142.M142.M142.M142.M142.M142.M142.
scala> { execute(tryFooBar02) ; work('M', 100) }
F142.F142.F142.F142.F142.F142.F142.F142.F142.F142.B142.B142.B142.B142.B142.B142.B142.
B142.B142.B142.barKoM142.M142.M142.M142.M142.M142.M142.M142.M142.M142.
scala> { execute(tryFooBar03) ; work('M', 100) }
F142.F142.F142.F142.F142.F142.F142.F142.F142.F142.fooKoM142.M142.M142.M142.M142.M142.
M142.M142.M142.M142.
scala> { execute(tryFooBar04) ; work('M', 100) }
F142.F142.F142.F142.F142.F142.F142.F142.F142.F142.fooKoM142.M142.M142.M142.M142.M142.
M142.M142.M142.M142.
```

So now that failure is dealt with for you, you are perfectly happy, right?
Well, maybe not. 
`bnd` deals with failure in a somewhat specific way using a _fail fast_ strategy.
Maybe what you want is a _fail slow_ strategy that _accumulates_ information about what went wrong along the way.

This is where `and` comes in.

`and` may be a better guide
---------------------------

Just as it is possible for computations of type `One` it is possible for computations of type `Try`
to define a method `and` that can be used to _compose computations_

``` scala
    // ...
    def and[Y](t_z2y: Try[Z => Y]): Try[Y] =
      (tz, t_z2y) match {
        case (Success(z), Success(z2y)) =>
          Try { z bind z2y }
        case (Success(_), Failure(t)) =>
          Failure(t)
        case (Failure(t), Success(_)) =>
          Failure(t)
        case (Failure(t1), Failure(t2)) =>
          Failure(new Throwable(
              t1.getMessage + 
              " and " +
              t2.getMessage))
      }
  }
```

Instead of relying on the default implementation of `and`
(that would use a fail fast strategy), you can take advantage of
the possibility to define `and` in another way to let it use a fail slow strategy
and accumulate error messages. All this is possible since `and`
is more abstract as `bnd` and, as such, allows for more implementation flexibility.

By the way: it is absolutely not the case that this is the one and only way
to use a fail slow strategy and accumulate information about what went wrong.
It is better to make use of a more powerful type, like the `Validation` type of ScalaZ. 
The point we want to make is that, when going for `and` as a guide,
you have more failure handling implementation flexibility for letting
`and` guide you along your way to a possible result. 

DSL for declaring computations of type `Try` (Part 2)
----------------------------------------------------- 

By now we have defined `mkTry`, `bnd` and `and`

* They constitute _a declarative DSL for describing computations_ of type `Try`

We have also defined `execute`

* It constitutes _an imperative DSL for executing computations_ of type `Try`

This section contains some examples on how to make use of those DSL's.

``` scala
lazy val tryFooBar05: Try[String] =
  mkTry { block("bar", true, 'B', 100) } and {
    mkTry { block("foo", true, 'F', 100) } and {
      mkTry(add)
    }
  }

lazy val tryFooBar06: Try[String] =
  mkTry { block("bar", true, 'B', 100) } and {
    mkTry { block("foo", false, 'F', 100) } and {
      mkTry(add)
    }
  }

lazy val tryFooBar07: Try[String] =
  mkTry { block("bar", false, 'B', 100) } and {
    mkTry { block("foo", true, 'F', 100) } and {
      mkTry(add)
    }
  }

lazy val tryFooBar08: Try[String] =
  mkTry { block("bar", false, 'B', 100) } and {
    mkTry { block("foo", false, 'F', 100) } and {
      mkTry(add)
    }
  }

// Exiting paste mode, now interpreting.

tryFooBar05: scala.util.Try[String] = <lazy>
tryFooBar06: scala.util.Try[String] = <lazy>
tryFooBar07: scala.util.Try[String] = <lazy>
tryFooBar08: scala.util.Try[String] = <lazy>
```

We can now, so to speak, try to print `fooOkbarOk` in a synchronous way

``` scala
scala> { execute(tryFooBar05) ; work('M', 100) }
B142.B142.B142.B142.B142.B142.B142.B142.B142.B142.F142.F142.F142.F142.F142.F142.F142.
F142.F142.F142.fooOkbarOkM142.M142.M142.M142.M142.M142.M142.M142.M142.M142.
scala> { execute(tryFooBar06) ; work('M', 100) }
B142.B142.B142.B142.B142.B142.B142.B142.B142.B142.F142.F142.F142.F142.F142.F142.F142.
F142.F142.F142.fooKoM142.M142.M142.M142.M142.M142.M142.M142.M142.M142.
scala> { execute(tryFooBar07) ; work('M', 100) }
B142.B142.B142.B142.B142.B142.B142.B142.B142.B142.F142.F142.F142.F142.F142.F142.F142.
F142.F142.F142.barKoM142.M142.M142.M142.M142.M142.M142.M142.M142.M142.
scala> { execute(tryFooBar08) ; work('M', 100) }
B142.B142.B142.B142.B142.B142.B142.B142.B142.B142.F142.F142.F142.F142.F142.F142.F142.
F142.F142.F142.barKo and fooKoM142.M142.M142.M142.M142.M142.M142.M142.M142.M142.
```

So far so good as far as _failure_ is concerned. But what about _latency_?
All code we have shown so far uses _synchronous, blocking_ computations.

Latency
-------

You all know that making use of _synchronous, blocking_ computations
is not the way to deal with latency.
In Java (and many other languages for that matter) _asynchronous, non-blocking_ computations
are used instead to deal with latency.
Asynchronous computations are often modeled using _callbacks_.

When asking programmers if they _like_ callbacks, many of them say they _hate_ them.

Why?

Well, because, when trying to deal with latency using callbacks, 
programmers are basically _left on their own_. 
In a way, when using callbacks, _they end up in callback hell_.

There must be a better way to deal with latency.

Computations of type `Future`
-----------------------------

A value of type `Try[Z]` that is defined as a `def`
is essentially the same as a value of type `Unit => Try[Z]`.
The _dual_ of such a value, obtained by reversing the arrow `=>`,
is a value of type `Try[Z] => Unit`. This dual is formalized by the
following type definition

``` scala
  type Callback[-Z] = Try[Z] => Unit
```

In a way (as illustrated in the following section) a `Future[Z]` value is the _dual_ of a `Callback[Z]` value,
and, as such, the _bi-dual_ of a `Try[Z]` value.

More precisely, in a way, `Future[Z]` is the same as `Callback[Z] => Unit`,
which is the same as `(Try[Z] => Unit) => Unit`.

This _bi-duality_ turns out to transform synchronous `Try` computations
into asynchronous `Future` computations.

Materializing (failure and) latency using `mkFuture`
----------------------------------------------------

A first step towards dealing with (failure and) latency is to write,
once and for all, code that _materializes (failure and) latency at compile time_

``` scala
  def mkFuture[Z]
    (cz2u: Callback[Z] => Unit)
    (implicit executionContext: ExecutionContext)
    : Future[Z] = {
    val pz = Promise[Z]()
    executionContext.prepare.execute(new Runnable {
      def run { { (tz: Try[Z]) => pz.complete(tz) ; () } bind cz2u }
    })
    pz.future
  }
```

The code above uses the `Promise[Z]` class in its implementation. 

You do not need to fully understand the _definition_ of `mkFuture`.
Think of it as the _dual_ of _fulfilling a promise_ in a separate thead, 
and returning that promise as a future.

In a way a `Promise[Z]` is both a `Callback[-Z]`,
since it has a method `complete` that has a parameter of type `Try[Z]`,
and a `Future[+Z]`,
since it has a variable `future` of type `Future[Z]`.

A promise is something that

* can be fulfilled (written) only once, in a thread safe way
* can be pushed (and read) many times.

It is possible, and often useful, to define futures directly in terms of promises.
This blog post does not go into details.

This definition of computations in terms of `mkFuture` is, in a way,
more complex than needed. For the moment, we are dealing
with reactive computations with _one_ value.
Reactive computations with _many_ values will be defined
in the next blog post a in similar way.
We want to emphasize this analogy.

When considering `mkFuture` as the _only_ way to
make a future, in a way, `Future[Z]` becomes the same as `Callback[Z] => Unit`.

A callback can be _registered_ to a future as shown below

``` scala
  implicit class FutureOps[Z](fz: Future[Z]) {
    def register(callback: Callback[Z]) {
      fz.onComplete { tz =>
        callback(tz)
      } 
    }
    // ...
``` 

The `Future` class of the standard Scala library already has an `onComplete` method 
that is equivalent to `register`. The definition of `register` is given for didactical reasons only.

It is important that you understand the _usage_ of `mkFuture`

* you can make a future by defining what it _pushes_ to registered callbacks

Not surprising, a computation of type `Try` can be transformed to a computation
of type `Future` using this idea

``` scala
  def try2future[Z](tz: => Try[Z]): Future[Z] =
    mkFuture { cz =>
      cz(tz)
    } 
```
The `try2future` code makes use of a _call by name_ parameter `tz`.

The example below illustrates how to use `try2future`

``` scala
  def mkFutureTry[Z](block: => Z): Future[Z] =
    try2future(mkTry { block })
```

The `mkTry` function for computations of type `Try` could naturally be reused.

The `mkFutureTry` code makes use of a _call by name_ parameter `block`
which you can think of as code that 
on the one hand, _can succeed_ or _can fail_ and,
on the other hand, _can take some time to be evaluated_.

How are computations of type `Future` described?

Describing Computations of type `Future`
----------------------------------------

Here are two computations of type `Future` that are described using `mkFutureTry`

``` scala
scala> lazy val okFutureTryFoo = mkFutureTry { block("foo", true, 'F', 100) }
okFutureTryFoo: scala.concurrent.Future[String] = <lazy>

scala> lazy val koFutureTryFoo = mkFutureTry { block("foo", false, 'B', 100) }
koFutureTryFoo: scala.concurrent.Future[String] = <lazy>
```

How are computations of type `Future` _executed_ once they are described?

Executing Computations of type `Future`
---------------------------------------

For the purpose of this series of blog posts,
executing a computation of type `Future` can be as simple as registering a callback
that either prints the value of a successful result pushed to it or prints the message of an unsuccessful result
pushed to it

``` scala
 def register[Z](fz: Future[Z]): Unit =
   fz.register { tz =>
     execute(tz)
   }
```

We have called the method `register` instead of `execute`
because it corresponds to a `register` method of `Future`

The `execute` function for computations of type `Try` could naturally be reused.

Below, the two computations of type `Future` are executed using `register`

``` scala
scala> { register(okFutureTryFoo) ; work('M', 120) }
F121.M142.F121.M142.F121.M142.F121.M142.F121.M142.F121.F121.M142.F121.M142.F121.M142.
F121.M142.fooOkM142.
scala> { register(okFutureTryFoo) ; work('M', 120) }
M142.fooOkM142.M142.M142.M142.M142.M142.M142.M142.M142.
scala> { register(koFutureTryFoo) ; work('M', 120) }
M142.B121.B121.M142.B121.M142.B121.M142.B121.M142.B121.M142.B121.B121.M142.B121.M142
.B121.M142.fooKoM142.
scala> { register(koFutureTryFoo) ; work('M', 120) }
M142.fooKoM142.M142.M142.M142.M142.M142.M142.M142.M142.
scala> 
```
So, what does materialized (failure and) latency bring you?

`bnd` is your guide
-------------------

Just as it is possible for computations of type `Try` it is possible for computations of type `Future` to define a method `bnd` that can be used to _compose computations_

``` scala 
    // ...
    def bnd[Y](z2fy: Z => Future[Y]): Future[Y] = {
      val py = Promise[Y]()
      fz.register {
        case Success(z) =>
          (z bind z2fy).register {
            case Success(y) => py.success(y)
            case Failure(t) => py.failure(t)
          }
        case Failure(t) =>
          py.failure(t)
      }
      py.future
    }
```

An important requirement for `z2fy` is that it must not throw exceptions. Using `mkFutureTry`, it is easy to fulfill this requirement.

With `bnd` in your toolbox, you are not on your own any more to deal with materialized (failure and) latency. Indeed, the method `bnd` is your trustworthy guide that

* _leads you through the successful execution path_
* _does the (failure and) latency related boilerplate work for you_

DSL for declaring computations of type `Future` (Part 1)
--------------------------------------------------------

By now we have defined `mkFutureTry` and `bnd`

* They constitute _a declarative DSL for describing computations_ of type `Future`

We have also defined `register`

* It constitutes _an imperative DSL for executing computations_ of type `Future`

This section contains some examples on how to make use of those DSL's.

``` scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

lazy val tryFutureFooBar01: Future[String] =
  mkFutureTry { block("foo", true, 'F', 100) } bnd { s =>
    mkFutureTry { block("bar", true, 'B', 100) } bnd { t =>
      mkFutureTry(add(s)(t))
    }
  }

lazy val tryFutureFooBar02: Future[String] =
  mkFutureTry { block("foo", true, 'F', 100) } bnd { s =>
    mkFutureTry { block("bar", false, 'B', 100) } bnd { t =>
      mkFutureTry(add(s)(t))
    }
  }

lazy val tryFutureFooBar03: Future[String] =
  mkFutureTry { block("foo", false, 'F', 100) } bnd { s =>
    mkFutureTry { block("bar", true, 'B', 100) } bnd { t =>
      mkFutureTry(add(s)(t))
    }
  }

lazy val tryFutureFooBar04: Future[String] =
  mkFutureTry { block("foo", false, 'F', 100) } bnd { s =>
    mkFutureTry { block("bar", false, 'B', 100) } bnd { t =>
      mkFutureTry(add(s)(t))
    }
  }

// Exiting paste mode, now interpreting.

tryFutureFooBar01: scala.concurrent.Future[String] = <lazy>
tryFutureFooBar02: scala.concurrent.Future[String] = <lazy>
tryFutureFooBar03: scala.concurrent.Future[String] = <lazy>
tryFutureFooBar04: scala.concurrent.Future[String] = <lazy>
```

We can now, so to speak, try to print `fooOkbarOk` in an asynchronous way

``` scala
scala> { register(tryFutureFooBar01) ; work('M', 300) }
F149.M142.F149.F149.F149.M142.F149.F149.M142.F149.F149.F149.M142.F149.B151.B151.M142.
B151.B151.B151.M142.B151.B151.B151.M142.B151.B151.fooOkbarOkM142.M142.M142.
scala> { register(tryFutureFooBar02) ; work('M', 300) }
F152.M142.F152.F152.M142.F152.F152.F152.M142.F152.F152.F152.M142.F152.B151.B151.M142.
B151.B151.B151.M142.B151.B151.B151.M142.B151.B151.barKoM142.M142.M142.
scala> { register(tryFutureFooBar03) ; work('M', 300) }
F151.M142.F151.F151.F151.M142.F151.F151.M142.F151.F151.F151.M142.F151.fooKoM142.M142.
M142.M142.M142.M142.
scala> { register(tryFutureFooBar04) ; work('M', 300) }
F151.M142.F151.F151.M142.F151.F151.F151.M142.F151.F151.F151.M142.F151.fooKoM142.M142.
M142.M142.M142.M142.
```

So now that latency is also dealt with for you, you are perfectly happy, right?
Well, maybe not.
`bnd` deals with latency in a somewhat specific (not to say sub-optimal) way using _sequential_ composition. Maybe what you want is a _parallel_ composition.

`and` may be a better guide
---------------------------

Just as it is possible for computations of type `Try` it is possible for computations of type `Future`
to define a method `and` that can be used to _compose computations_

``` scala
    def and[Y](f_z2y: Future[Z => Y]): Future[Y] = {
      val py = Promise[Y]()
      fz.register { tz =>
        f_z2y.register { t_z2y =>
          py.complete(tz and t_z2y)
        }
      }
      py.future
    }
```

The `and` method for computations of type `Try` could naturally be reused.

Instead of relying on the default implementation of `and`
(that would use sequential composition), you can take advantage of
the possibility to define `and` in another way to let it use parallel composition
All this is possible since `and`
is more abstract as `bnd` and, as such, allows for more implementation flexibility.

The point we want to make is that, when going for `and` as a guide,
you have more latency handling implementation flexibility for letting
`and` guide you along your way to a possible result. 

DSL for declaring computations of type `Try` (Part 2)
----------------------------------------------------- 

By now we have defined `mkFutureTry`, `bnd` and `and`

* They constitute _a declarative DSL for describing computations_ of type `Try`

We have also defined `register`

* It constitutes _an imperative DSL for executing computations_ of type `Try`

This section contains some examples on how to make use of those DSL's.

``` scala
// Entering paste mode (ctrl-D to finish)

lazy val tryFutureFooBar05: Future[String] =
  mkFutureTry { block("bar", true, 'B', 50) } and {
    mkFutureTry { block("foo", true, 'F', 100) } and {
      mkFutureTry(add)
    }
  }

lazy val tryFutureFooBar06: Future[String] =
  mkFutureTry { block("bar", true, 'B', 50) } and {
    mkFutureTry { block("foo", false, 'F', 100) } and {
      mkFutureTry(add)
    }
  }

lazy val tryFutureFooBar07: Future[String] =
  mkFutureTry { block("bar", false, 'B', 50) } and {
    mkFutureTry { block("foo", true, 'F', 100) } and {
      mkFutureTry(add)
    }
  }

lazy val tryFutureFooBar08: Future[String] =
  mkFutureTry { block("bar", false, 'B', 50) } and {
    mkFutureTry { block("foo", false, 'F', 100) } and {
      mkFutureTry(add)
    }
  }

// Exiting paste mode, now interpreting.

tryFutureFooBar05: scala.concurrent.Future[String] = <lazy>
tryFutureFooBar06: scala.concurrent.Future[String] = <lazy>
tryFutureFooBar07: scala.concurrent.Future[String] = <lazy>
tryFutureFooBar08: scala.concurrent.Future[String] = <lazy>
```

We can now, so to speak, try to print `fooOkbarOk` in an asynchronous way

``` scala
scala> { register(tryFutureFooBar05) ; work('W', 150) }
B149.F154.W142.B149.B149.F154.B149.W142.F154.B149.B149.F154.B149.W142.B149.F154.B149.
W142.B149.F154.F154.W142.F154.W142.F154.W142.F154.fooOkbarOkW142.W142.W142.
scala> { register(tryFutureFooBar06) ; work('W', 150) }
B154.F155.W142.B154.B154.F155.B154.W142.F155.B154.B154.F155.W142.B154.B154.F155.B154.
W142.B154.F155.W142.F155.F155.W142.F155.W142.F155.fooKoW142.W142.W142.
scala> { register(tryFutureFooBar07) ; work('W', 150) }
B154.F149.W142.B154.B154.F149.B154.W142.F149.B154.B154.F149.W142.B154.B154.F149.B154
.W142.B154.F149.W142.F149.F149.W142.F149.W142.F149.barKoW142.W142.W142.
scala> { register(tryFutureFooBar08) ; work('W', 150) }
B149.F155.W142.B149.B149.F155.B149.W142.F155.B149.B149.F155.W142.B149.B149.F155.B149.
W142.B149.F155.W142.F155.F155.W142.F155.W142.F155.barKo and fooKoW142.W142.W142.
```

In the examples above,
the `block` using `"bar"` is executed faster than
the `block` using `"foo"`. 
So what about the opposite?

``` scala
// Entering paste mode (ctrl-D to finish)

lazy val tryFutureFooBar09: Future[String] =
  mkFutureTry { block("bar", true, 'B', 100) } and {
    mkFutureTry { block("foo", true, 'F', 50) } and {
      mkFutureTry(add)
    }
  }

lazy val tryFutureFooBar10: Future[String] =
  mkFutureTry { block("bar", true, 'B', 100) } and {
    mkFutureTry { block("foo", false, 'F', 50) } and {
      mkFutureTry(add)
    }
  }

lazy val tryFutureFooBar11: Future[String] =
  mkFutureTry { block("bar", false, 'B', 100) } and {
    mkFutureTry { block("foo", true, 'F', 50) } and {
      mkFutureTry(add)
    }
  }

lazy val tryFutureFooBar12: Future[String] =
  mkFutureTry { block("bar", false, 'B', 100) } and {
    mkFutureTry { block("foo", false, 'F', 50) } and {
      mkFutureTry(add)
    }
  }

// Exiting paste mode, now interpreting.

tryFutureFooBar09: scala.concurrent.Future[String] = <lazy>
tryFutureFooBar10: scala.concurrent.Future[String] = <lazy>
tryFutureFooBar11: scala.concurrent.Future[String] = <lazy>
tryFutureFooBar12: scala.concurrent.Future[String] = <lazy>
```

We can now, again, so to speak, try to print `fooOkbarOk` in an asynchronous way

``` scala
scala> { register(tryFutureFooBar09) ; work('W', 150) }
B155.F157.W142.F157.B155.F157.F157.W142.B155.F157.F157.B155.W142.F157.F157.B155.F157.
W142.F157.B155.B155.W142.B155.W142.B155.B155.W142.fooOkbarOkW142.W142.W142.
scala> { register(tryFutureFooBar10) ; work('W', 150) }
B155.F159.W142.F159.B155.F159.W142.F159.B155.F159.F159.B155.W142.F159.F159.B155.F159.
W142.F159.B155.B155.W142.B155.W142.B155.W142.B155.fooKoW142.W142.W142.
scala> { register(tryFutureFooBar11) ; work('W', 150) }
B155.F158.W142.F158.B155.F158.F158.W142.B155.F158.F158.B155.W142.F158.F158.B155.F158.
W142.F158.B155.B155.W142.B155.W142.B155.W142.B155.barKoW142.W142.W142.
scala> { register(tryFutureFooBar12) ; work('W', 150) }
B155.F158.W142.F158.B155.F158.W142.F158.B155.F158.F158.B155.W142.F158.F158.B155.F158.
W142.F158.B155.W142.B155.B155.W142.B155.W142.B155.barKo and fooKoW142.W142.W142.
```

In the examples above,
the `block` using `"foo"` is executed faster than
the `block` using `"bar"`.

 



