---
layout: post
title: "2014 JavaOne University: Functional Programming in Scala"
date: 2014-10-23	 09:00:00 +0200
author: Luc Duponcheel
comments: true
---

This blog post correponds to my 2014 JavaOne University course.

The content of this blog post (deliberately) does not go into all details.

<!-- more -->

Course content
--------------

This course is about _functional programming patterns_ in general,
and functional programming patterns _related to datasources_ in particular.

foo

Course approach
---------------

The course describes a _Scala library_ that formalizes functional datasource patterns. 

Back to school
--------------

One of the main goals of this course is to illustrate that datasources are closely related to concepts you learned in school.

Multiplication
--------------

Most probably, you remember the _multiplication_ operator

* `*` for integers
* `and` for booleans
* ...

Most probably, you also remember that the multiplication operators above have a _neutral_ element

* `1` for integers
* `true` for booleans
* ...

Multiplicative data
-------------------

At this moment we do a big (not to say gigantic) step forwards by, somehow, _adding data_ to the multiplication operator above. An important question to answer is: what should be the _data semantics_ associated with multiplication?

_Multiplicative data_ are formalized by the `trait`'s below

{% include_code JavaOneUniversity2014_HasOne lang:scala JavaOneUniversity2014_HasOne.scala %}

{% include_code JavaOneUniversity2014_Multiplicative lang:scala JavaOneUniversity2014_Multiplicative.scala %}

where _Runnable_ is formalized by the `trait` below

{% include_code JavaOneUniversity2014_Runnable lang:scala JavaOneUniversity2014_Runnable.scala %}

and the _Product Type_ `Z ** Y` is defined as

{% include_code JavaOneUniversity2014_Product lang:scala JavaOneUniversity2014_Product.scala %}

`trait HasOneModule`

* declares the type `D[Z]` as a subtype of `Multiplicative[Z]`
* declares the function `one`
    * given a value `z` of type `Z`, `one(z)` should produce `z`

`trait Multiplicative[A]` 

* declares the method `and`
    * given `da`, somehow producing values of type `A`, and `d_a2b`, somehow producing values of type `A => B`, `da and d_a2b` should, somehow, using the values produced by `da` and `d_a2b`, produce values of type `B`
* defines the method `end` in terms of `and` and `one` 
* defines the method `**` in terms of `and` and `end` 

_Note:_ This "declares the type `D[Z]` as a subtype of `Something[Z]`" is such a common theme that we are not going to repeat it over and over again.

Domain Specific Language
------------------------

The functions and methods of `MultiplicativeModule` constitute a _DSL_.

_Note:_ This "the functions and methods of `SomethingModule` constitute a _DSL_" is such a common theme that we are not going to repeat it over and over again.

Multiplicative data usage
-------------------------

The code below defines some examples that make use of the multiplicative data DSL

{% include_code JavaOneUniversity2014_MultiplicativeUsage lang:scala JavaOneUniversity2014_MultiplicativeUsage.scala %}

Think about expressions like `one(i1) ** one(i2) ** di`
as generalizations of expressions like `a * b * x`
that also _produce accumulated data_.

Above we defined _abstract_ examples that make use of the multiplicative data DSL
before defining any _concrete_ multiplicative data _instance_.
Think of it as _programming to an interface_.

Below is a trivial `Runnable` implementation

{% include_code JavaOneUniversity2014_TrivialRunnableModule lang:scala JavaOneUniversity2014_TrivialRunnableModule.scala %}

The simplest `MultiplicativeModule` instance one can think of is one along the following lines

``` scala
    object OneModule {
      // ...
      case class One[A](a: A) {
      // ...
      }
    }
```

Think of `One[Z]` as a data source that produces _one value_.

Given a `One[Z]` instance it is possible to define

{% include_code JavaOneUniversity2014_OneMultiplicativeUsage lang:scala JavaOneUniversity2014_OneMultiplicativeUsage.scala %}

and use it as follows

{% include_code JavaOneUniversity2014_OneMultiplicativeUsageInApp lang:scala JavaOneUniversity2014_OneMultiplicativeUsageInApp.scala %}

Running the examples above would then result in something along the following lines

``` scala
{ (1,2) }
{ ((1,2),3) }
{ (((1,2),3),4) }
{ (1,(2,3)) }
{ 3 }
{ 6 }
{ 10 }
```

Addition
--------

Most probably, you remember the _addition_ operator

* `+` for integers
* `or` for booleans
* ...

Most probably, you also remember that the addition operators above have a _neutral_ element

* `0` for integers
* `false` for booleans
* ...

Additive data
-------------

Again, an important question to answer is: what should be the data semantics associated with addition be?

_Additive data_ are formalized by the `trait`'s below

{% include_code JavaOneUniversity2014_HasZero lang:scala JavaOneUniversity2014_HasZero.scala %}

{% include_code JavaOneUniversity2014_Additive lang:scala JavaOneUniversity2014_Additive.scala %}

where the _Sum Type_ `Z ++ Y` is defined as

{% include_code JavaOneUniversity2014_Sum lang:scala JavaOneUniversity2014_Sum.scala %}

`trait HasZeroModule` 

* declares the function `zero`
    * `zero(())` should produce no values

`trait Additive[A]` 
    
* declares the method `or`
    * given `da`, somehow producing values of type `A`, and `da1`, somehow producing values of type `A`, `da or da1` should, somehow, produce the values produced by `da` or produce the values produced by `da1`
* declares the method `++`, defined later in terms of (among others) `or`

_Note:_
Do not think of `or` as an _exclusive_ one, think of `or` as an _inclusive_ one.

_Note:_
`Additive[A]` also has a method `take`

* when making use of _recursion_, _infinitely many values_ can be produced

Polynomial data
---------------

_Polynomial data_ are formalized by the `trait` below
as a combination of Multiplicative data and Additive data

{% include_code JavaOneUniversity2014_Polynomial lang:scala JavaOneUniversity2014_Polynomial.scala %}

`trait Polynomial[A]` 
    
* defines the method `++` in terms of `end` and `or`

Polynomial data usage
-------------------------

The code below defines some examples that make use of the polynomial data DSL

{% include_code JavaOneUniversity2014_PolynomialUsage lang:scala JavaOneUniversity2014_PolynomialUsage.scala %}

Again, think about expressions like `one(i1) ** one(i2) ++ (one(i1) ++ one(i2)) ** di ++ di ** di`, resp. `(one(i1) ++ di) ** (one(i2) ++ di)`
as generalizations of expressions like `a * b + (a + b) * x + x * x` resp. `(a + x) * (b + x)`
that also _produce accumulated data_.

The simplest `PolynomialModule` instance one can think of is one along the following lines

``` scala
    object ZeroOrOneModule {
      // ...
      sealed trait ZeroOrOne[A] {
      // ...
      }
      private case class Zero[A]() extends ZeroOrOne[A]()
      private case class One[A](a: A) extends ZeroOrOne[A]
    }
```

Think of `ZeroOrOne[Z]` as a data source that produces _zero or one values_.

Given a `ZeroOrOne[Z]` instance it is possible to define

{% include_code JavaOneUniversity2014_ZeroOrOnePolynomialUsage lang:scala JavaOneUniversity2014_ZeroOrOnePolynomialUsage.scala %}

and use it as follows

{% include_code JavaOneUniversity2014_ZeroOrOnePolynomialUsageInApp lang:scala JavaOneUniversity2014_ZeroOrOnePolynomialUsageInApp.scala %}

Running the examples above would then result in something along the following lines

``` scala
{ (1,2) }
{ (1,2) }
{ (1,2) }
{ (3,3) }
{ (1,2) }
{ (1,2) }
{ (1,2) }
{ (3,3) }
{ Left(Left((1,2))) }
{ Left(Left((1,2))) }
{ Right((1,2)) }
{ Left(Left((3,3))) }
{ (Left(1),Left(2)) }
{ (Left(1),Left(2)) }
{ (Right(1),Right(2)) }
{ (Left(3),Left(3)) }
{ 0 }
{ 0 }
```

A more complex `PolynomialModule` instance is one along the following lines

``` scala
    object ZeroOrMoreModule {
      // ...
      sealed trait ZeroOrMore[A] {
      // ...
      }
      private case class Zero[A]() extends ZeroOrMore[A]
      private case class More[A](a_n_u2da: A ** (Unit => ZeroOrMore[A])) extends ZeroOrMore[A]
    }
```

Think of `ZeroOrMore[Z]` as a data source that produces _zero or more values_.

Given a `ZeroOrMore[Z]` instance it is possible to define

{% include_code JavaOneUniversity2014_ZeroOrMorePolynomialUsage lang:scala JavaOneUniversity2014_ZeroOrMorePolynomialUsage.scala %}

and use it as follows

{% include_code JavaOneUniversity2014_ZeroOrMorePolynomialUsageInApp lang:scala JavaOneUniversity2014_ZeroOrMorePolynomialUsageInApp.scala %}

Running the examples above would then result in something along the following lines

``` scala
[ (1,2) ]
[ (1,2) (1,3) (2,3) (3,3) ]
[ (1,2) ]
[ (3,3) (3,1) (3,2) (1,2) ]
[ (1,2) ]
[ (1,2) (1,3) (3,2) (3,3) ]
[ (1,2) ]
[ (3,3) (3,2) (1,3) (1,2) ]
[ Left(Left((1,2))) ]
[ Left(Left((1,2))) Left(Right((Left(1),3))) Left(Right((Right(2),3))) Right((3,3)) ]
[ Right((1,2)) ]
[ Left(Left((3,3))) Left(Right((3,Left(1)))) Left(Right((3,Right(2)))) Right((1,2)) ]
[ (Left(1),Left(2)) ]
[ (Left(1),Left(2)) (Left(1),Right(3)) (Right(3),Left(2)) (Right(3),Right(3)) ]
[ (Right(1),Right(2)) ]
[ (Left(3),Left(3)) (Left(3),Right(2)) (Right(1),Left(3)) (Right(1),Right(2)) ]
[ 0 1 2 3 4 5 6 7 8 9 ]
[ 0 1 1 2 3 5 8 13 21 ]
```

Datasource
----------

You are, most probably, used to think of 

* _functions_ in terms of _applying_ them to _values_ as in `a2b(a)` or, equivalently, `a2b apply a`

You can also think of

* _values_ in terms of _binding_ them to _functions_ as in `a bind a2b`

The method `bind` below, defined in terms of `apply`, formalizes binding

{% include_code JavaOneUniversity2014_Bind lang:scala JavaOneUniversity2014_Bind.scala %}

_Data sources_, formalized by the `trait` below, generalize binding

{% include_code JavaOneUniversity2014_Source lang:scala JavaOneUniversity2014_Source.scala %}

where the _Predicate Type_ `Predicate[Z]` is defined as

{% include_code JavaOneUniversity2014_Predicate lang:scala JavaOneUniversity2014_Predicate.scala %}

_Note:_ We made use of `bind` just to show that it is as simple to use as `apply`.

`trait SourceModule` 

* defines the function `join` in terms of `bnd`

`trait Source[A]` 

* declares the method `bnd`
    * given `da`, somehow producing values of type `A`, and `a2db` of type `A => D[B]`, `da bnd a2db` should, somehow, using `a2b` and the values produced by `da`, produce values of type `B`
* defines the method `and` in terms of `bnd` and `one`
* defines the method `switch` in terms of `bnd`

The definitions of `join` and `and` are the most natural ones you can possibly come up with to get the types right.

About abstraction, power of expression and implementation flexibility
---------------------------------------------------------------------

_abstraction_

`and` is more abstract than `bnd`

* `and` is defined interms of `bnd`

_power of expression_
 
`bnd`, being less abstract, has more power of expression than `and`

* When making use of `bnd`, values are immediately in scope from the outermost scope all the way down to the innermost scope.
* When making use of `and`, values are only in scope at the innermost scope.

The method `switch` illustrates this power of expression: `a` can be used to decide how to proceed.

_implementation flexibility_

`and`, being more abstract, has more implementation flexibility than `bnd`

* The definition of `and` is a _default_ definition that can be overridden for specific implementations.

Datasource usage
----------------

The code below defines some examples that make use of the datasource DSL

{% include_code JavaOneUniversity2014_SourceUsage lang:scala JavaOneUniversity2014_SourceUsage.scala %}

Given a `One` instance it is possible to define

{% include_code JavaOneUniversity2014_OneSourceUsage lang:scala JavaOneUniversity2014_OneSourceUsage.scala %}

and use it as follows

{% include_code JavaOneUniversity2014_OneSourceUsageInApp lang:scala JavaOneUniversity2014_OneSourceUsageInApp.scala %}

Running the examples above would then result in something along the following lines

``` scala
{ (1,2) }
{ ((1,2),3) }
{ (((1,2),3),4) }
{ 5 }
{ 121 }
```

Polynomial datasource
---------------------

_Polynomial datasources_ are formalized by the `trait` below
as a combination of Datasources and Polynomial data

{% include_code JavaOneUniversity2014_PolynomialSource lang:scala JavaOneUniversity2014_PolynomialSource.scala %}

`trait PolynomialSource[A]` 
    
* defines the method `filter` in terms of `switch`, `one` and `zero`

Again, the definition of `filter` is the most natural one you can possibly come up with to get the type right.

Polynomial datasource usage
---------------------------

The code below defines some examples that make use of the polynomial datasource DSL

{% include_code JavaOneUniversity2014_PolynomialSourceUsage lang:scala JavaOneUniversity2014_PolynomialSourceUsage.scala %}

Given a `ZeroOrOne` instance it is possible to define

{% include_code JavaOneUniversity2014_ZeroOrOnePolynomialSourceUsage lang:scala JavaOneUniversity2014_ZeroOrOnePolynomialSourceUsage.scala %}

and use it as follows

{% include_code JavaOneUniversity2014_ZeroOrOnePolynomialSourceUsageInApp lang:scala JavaOneUniversity2014_ZeroOrOnePolynomialSourceUsageInApp.scala %}

Running the examples above would then result in something along the following lines

``` scala
{ (1,2) }
{ (3,3) }
{ Right((1,2)) }
{ Left(Left((3,3))) }
{ }
{ (3,3) }
{ Right((1,2)) }
{ }
```

Given a `ZeroOrMore` instance it is possible to define

{% include_code JavaOneUniversity2014_ZeroOrMorePolynomialSourceUsage lang:scala JavaOneUniversity2014_ZeroOrMorePolynomialSourceUsage.scala %}

and use it as follows

{% include_code JavaOneUniversity2014_ZeroOrMorePolynomialSourceUsageInApp lang:scala JavaOneUniversity2014_ZeroOrMorePolynomialSourceUsageInApp.scala %}

Running the examples above would then result in something along the following lines

``` scala
[ (1,2) ]
[ (3,3) (3,1) (3,2) (1,2) ]
[ Right((1,2)) ]
[ Left(Left((3,3))) Left(Right((3,Left(1)))) Left(Right((3,Right(2)))) Right((1,2)) ]
[ ]
[ (3,3) ]
[ Right((1,2)) ]
[ Right((1,2)) ]
```

Reducible data
---------------

_Reducible data_ are formalized by the `trait` below

{% include_code JavaOneUniversity2014_Reducible lang:scala JavaOneUniversity2014_Reducible.scala %}

`trait ReducibleModule` 

* declares the type `Reducer[Z, R]`
* declares the reducer `identityReducer`
    * given `dz`, `dz reducedBy identityReducer` should reduce to `dz`

`trait Reducible[A]` 

* declares the method `reducedBy`
    * given `da`, somehow producing values of type `A`, and `a_r2_r`, somehow reducing those values to a result of type `R`, `da reducedBy a_r2_r` should reduce those values to that result

Traversable data
----------------

_Traversable data_ are formalized by the `trait` below

{% include_code JavaOneUniversity2014_Traversable lang:scala JavaOneUniversity2014_Traversable.scala %}

`trait TraversableModule`

* defines the type `MM` as an alias of the type `MultiplicativeModule`
* declares the function `lift`
    * given an `mm` of type `MM` and a function `z2mmy` of type `Z => mm.D[Y]`, `lift` should _lift_ a reducer of type `Reducer[Y, R]` to a reducer of type `Reducer[Z, mm.D[R]]`
* defines the function `swap`, in terms of `foreach`, `identity` and `identityReducer`

`trait Traversable[A]`

* defines the method `foreach` in terms of `lift` and `reducedBy`

Again, the definitions of `swap` and `foreach` are the most natural ones you can possibly come up with to get the type right.

_Traversable polynomial data_ are formalized by the `trait` below

{% include_code JavaOneUniversity2014_TraversablePolynomial lang:scala JavaOneUniversity2014_TraversablePolynomial.scala %}

_Traversable datasources_ are formalized by the `trait` below

{% include_code JavaOneUniversity2014_TraversableSource lang:scala JavaOneUniversity2014_TraversableSource.scala %}

_Traversable polynomial datasources_ are formalized by the `trait` below

{% include_code JavaOneUniversity2014_TraversablePolynomialSource lang:scala JavaOneUniversity2014_TraversablePolynomialSource.scala %}

Traversable polynomial data (and polynomial data) usage
-------------------------------------------------------

The code below defines some examples that make use of the traversable polynomial data (and polynomial data) DSL

{% include_code JavaOneUniversity2014_TraversablePolynomialAndPolynomialUsage lang:scala JavaOneUniversity2014_TraversablePolynomialAndPolynomialUsage.scala %}

Given `ZeroOrMore` and `ZeroOrOne` instances it is possible to define

{% include_code JavaOneUniversity2014_ZeroOrMoreAndZeroOrOneUsage lang:scala JavaOneUniversity2014_ZeroOrMoreAndZeroOrOneUsage.scala %}

and use it as follows

{% include_code JavaOneUniversity2014_ZeroOrMoreAndZeroOrOneUsageInApp lang:scala JavaOneUniversity2014_ZeroOrMoreAndZeroOrOneUsageInApp.scala %}

Running the examples above would then result in something along the following lines

``` scala
[ { 1 } { 2 } { 3 } ]
{ [ 1 2 3 ] }
[ { } { 2 } { 3 } ]
{ }
[ { 1 } { } { } ]
{ }
```

_Note_ We swapped to a non-zero result if and only if all values are non-zero.

as such

* `ZeroOrOne` could be used as a _success or failure_ mechanism

but

* `ZeroOrOne` should be used for what it is meant for: _zero_ or _one_ values

Failure handling strategies
---------------------------

Below are two failure handling strategies

* _fail fast_
* _fail slow_ and _accumulate_ failure information

Monoids
-------

Accumulation is modeled by the _monoid_ concept.

_Monoids_ are formalized by the `trait` below

{% include_code JavaOneUniversity2014_Monoid lang:scala JavaOneUniversity2014_Monoid.scala %}

`trait MonoidModule` 

* declares the type `M` as a subtype of `Monoid`
* declares the `neutral` element of type `M`

`trait Monoid` 

* declares the method `operator`
    * given an element `m` of type `M` and an element `m1` of type `M`, `m operator m1` should be an element of type `M`

_Strings as monoids_ are formalized by the instance below

{% include_code JavaOneUniversity2014_StringMonoid lang:scala JavaOneUniversity2014_StringMonoid.scala %}

Failing data
------------

_Failing data_ are formalized by the `trait` below

{% include_code JavaOneUniversity2014_Failing lang:scala JavaOneUniversity2014_Failing.scala %}

`trait FailingModule` 

* declares a `mm` of type `MonoidModule` and does an appropriate import
* declares the function `fail`
    * given an element `m` of type `M`, `fail(m)` should, somehow, not yield any values, but fail instead with failure information `m`
* defines the function `zero` in terms of `fail` and `neutral`

_Polynomial failing data_ are formalized by the `trait` below

{% include_code JavaOneUniversity2014_PolynomialFailing lang:scala JavaOneUniversity2014_PolynomialFailing.scala %}

The simplest `PolynomialFailingModule` instance one can think of is one along the following lines

``` scala
    trait SuccessOrFailureModule {
      // ...
      sealed trait SuccessOrFailure[A] {
      // ...
      }
      private case class Failure[A](m: M) extends SuccessOrFailure[A]
      private case class Success[A](a: A) extends SuccessOrFailure[A]
    }
```

Think of `SuccessOrFailure[Z]` as a data source that produces _one value_ or _fails_.

_Note:_ `SuccessOrFailureModule` is a `trait` (`mm` still needs to be defined).

The simplest `SuccessOrFailureModule` instance one can think of is the following

{% include_code JavaOneUniversity2014_StringSuccessOrFailureModule lang:scala JavaOneUniversity2014_StringSuccessOrFailureModule.scala %}

Traversable polynomial data (and polynomial failing data) usage
---------------------------------------------------------------

The code below defines some examples that make use of the traversable polynomial data (and polynomial failing data) DSL

{% include_code JavaOneUniversity2014_TraversablePolynomialAndPolynomialFailingUsage lang:scala JavaOneUniversity2014_TraversablePolynomialAndPolynomialFailingUsage.scala %}

Given `ZeroOrMore` and `StringSuccessOrFailure` instances it is possible to define

{% include_code JavaOneUniversity2014_ZeroOrMoreAndStringSuccessOrFailureUsage lang:scala JavaOneUniversity2014_ZeroOrMoreAndStringSuccessOrFailureUsage.scala %}

and use it as follows

{% include_code JavaOneUniversity2014_ZeroOrMoreAndStringSuccessOrFailureUsageInApp lang:scala JavaOneUniversity2014_ZeroOrMoreAndStringSuccessOrFailureUsageInApp.scala %}

Running the examples above would then result in something along the following lines

``` scala
[ { 1 } { 2 } { 3 } ]
{ [ 1 2 3 ] }
[ { failure1 } { 2 } { 3 } ]
{ failure1 }
[ { 1 } { failure1 } { failure2 } ]
{ failure1 failure2 }
```

If, as suggested in the code of the actual implementation (not given yet), you comment out the _overriding definition_ of `and` and make use of the _default definition_ instead, then this results in something along the following lines

``` scala
[ { 1 } { 2 } { 3 } ]
{ [ 1 2 3 ] }
[ { failure1 } { 2 } { 3 } ]
{ failure1 }
[ { 1 } { failure1 } { failure2 } ]
{ failure1 }
```

_What is the moral of this story:_

* `and`, being more abstract than `bnd`, has more implementation flexibility

The default fail fast strategy implementation can (but does not have to) be overridden by a fail slow and accumulate strategy implementation.

Stateful data
-------------

_Stateful data_ are formalized by the `trait` below

{% include_code JavaOneUniversity2014_Stateful lang:scala JavaOneUniversity2014_Stateful.scala %}

`trait StatefulModule` 

* declares a type `S`
    * `S` represents _state_
* declares the function `get`
    * `get(())` should, somehow, produce the current state
* declares the function `set`
    * `set(s)` should, somehow, set the current state (and produce `()`)
* declares the function `exec`
    * given a function `s2s`, `exec(s2s)` should, somehow, transform the current state `s` to `s2s(s)` (and produce `()`)

Stateful data source
--------------------

_Stateful datasources_ are formalized by the `trait` below

{% include_code JavaOneUniversity2014_StatefulSource lang:scala JavaOneUniversity2014_StatefulSource.scala %}

`trait StatefulSourceModule`

* defines the function `exec` in terms of `get`, `set` and `bnd`

Stateful datasource usage
-------------------------

The code below defines some example that makes use of the stateful datasource DSL.
It deals with a simple "coin and candy" state machine.

{% include_code JavaOneUniversity2014_StatefulSourceUsage lang:scala JavaOneUniversity2014_StatefulSourceUsage.scala %}

The simplest stateful `Runnable` implementation is the following

{% include_code JavaOneUniversity2014_StatefulRunnableModule lang:scala JavaOneUniversity2014_StatefulRunnableModule.scala %}

The simplest `StatefulSourceModule` instance one can think of is one along the following lines

``` scala
    object StateModule {
      // ...
      case class State[A](s_2_sna: S => S ** A) {
      // ...
      }
    }
```

Think of `State[Z]` as a data source that produces, _maintaining state while producing_, one value.

Given a `State[Z]` instance it is possible to define

{% include_code JavaOneUniversity2014_StateUsage lang:scala JavaOneUniversity2014_StateUsage.scala %}

and use it as follows

{% include_code JavaOneUniversity2014_StateUsageInApp lang:scala JavaOneUniversity2014_StateUsageInApp.scala %}

Running the example above would then result in something along the following lines

``` scala
Machine(FREE,18,2)
```

Asynchronous data
-----------------

_Asynchronous data_ are formalized by the `trait` below

{% include_code JavaOneUniversity2014_Asynchronous lang:scala JavaOneUniversity2014_Asynchronous.scala %}

`trait AsynchronousModule`
    
* declares the function `async` 
    * given `u2z`, `async(u2z)` should produce `u2z(())` _asynchronously_

Asynchronous data source
------------------------

_Asynchronous datasources_ are declared by the `trait` below

{% include_code JavaOneUniversity2014_AsynchronousSource lang:scala JavaOneUniversity2014_AsynchronousSource.scala %}

Asynchronous datasource usage
-----------------------------

The code below defines some examples that make use of the asynchronous datasource DSL

{% include_code JavaOneUniversity2014_AsynchronousSourceUsage lang:scala JavaOneUniversity2014_AsynchronousSourceUsage.scala %}

The simplest asynchronous `Runnable` implementation is the following

{% include_code JavaOneUniversity2014_AsynchronousRunnableModule lang:scala JavaOneUniversity2014_AsynchronousRunnableModule.scala %}

The simplest `AsynchronousSourceModule` instance one can think of is one along the following lines

``` scala
    object AsyncModule {
      // ...
      case class Async[A](es_2_sfa: ExecutorService => SimpleFuture[A]) {
      // ...
      }
    }
```

Think of `Async[Z]` as a data source that, making use of an _executor service_, produces one value _asynchronously_.

Given a `Async[Z]` instance it is possible to a define

{% include_code JavaOneUniversity2014_AsyncUsage lang:scala JavaOneUniversity2014_AsyncUsage.scala %}

and use it as follows

{% include_code JavaOneUniversity2014_AsyncUsageInApp lang:scala JavaOneUniversity2014_AsyncUsageInApp.scala %}

Running the example above would then result in something along the following lines

``` scala
2 3 4 1 6 5 7 8 2 6 1 4 8 7 3 5 2 1 6 4 8 7 3 5 2 6 8 7 1 4 3 5 2 6 8 3 4 7 1 5 2 6 3 8 7 1 4 5 2 8 3 6 5 4 1 7 2 8 3 6 1 5 7 4 2 3 8 6 1 5 4 7 
3 finished

6 finished

8 finished

2 finished

1 finished

7 finished

5 finished

4 finished
[ 1 2 3 4 5 6 7 8 ]
7 6 6 2 4 8 3 7 1 4 5 6 1 8 3 2 3 8 1 1 6 5 1 7 4 2 2 6 7 1 6 3 8 5 7 2 2 6 4 3 7 3 6 3 8 8 4 1 2 6 5 3 
6 finished
7 8 8 5 4 3 1 2 
3 finished
5 7 2 8 
8 finished
4 1 5 7 
2 finished
5 4 
1 finished

7 finished
5 4 
4 finished

5 finished
[ 1 2 3 4 5 6 7 8 ]
```

If, as suggested in the code of the actual implementation (not given yet), you comment out the _overriding definition_ of `and` and make use of the _default definition_ instead, then this results in something along the following lines

``` scala
1 1 1 1 1 1 1 1 1 
1 finished
2 2 2 2 2 2 2 2 2 
2 finished
3 3 3 3 3 3 3 3 3 
3 finished
4 4 4 4 4 4 4 4 4 
4 finished
5 5 5 5 5 5 5 5 5 
5 finished
6 6 6 6 6 6 6 6 6 
6 finished
7 7 7 7 7 7 7 7 7 
7 finished
8 8 8 8 8 8 8 8 8 
8 finished
[ 1 2 3 4 5 6 7 8 ]
1 1 1 1 1 1 1 1 1 
1 finished
2 2 2 2 2 2 2 2 2 
2 finished
3 3 3 3 3 3 3 3 3 
3 finished
4 4 4 4 4 4 4 4 4 
4 finished
5 5 5 5 5 5 5 5 5 
5 finished
6 6 6 6 6 6 6 6 6 
6 finished
7 7 7 7 7 7 7 7 7 
7 finished
8 8 8 8 8 8 8 8 8 
8 finished
[ 1 2 3 4 5 6 7 8 ]
```

_What is the moral of this story:_

* `and`, being more abstract than `bnd`, has more implementation flexibility

The default _sequential_ implementation can (but does not have to) be overridden by a _concurrent_ implementation.

Implementations
---------------

We still did not fully define any concrete instances yet.

OneModule
---------

`OneModule` is a `TraversableSourceModule`

{% include_code JavaOneUniversity2014_OneModule lang:scala JavaOneUniversity2014_OneModule.scala %}

ZeroOrOneModule
---------------

`ZeroOrOneModule` is a `TraversablePolynomialSourceModule`

{% include_code JavaOneUniversity2014_ZeroOrOneModule lang:scala JavaOneUniversity2014_ZeroOrOneModule.scala %}

ZeroOrMoreModule
---------------

`ZeroOrMoreModule` is a `TraversablePolynomialSourceModule`

{% include_code JavaOneUniversity2014_ZeroOrMoreModule lang:scala JavaOneUniversity2014_ZeroOrMoreModule.scala %}

SuccessOrFailureModule
----------------------

`SuccessOrFailureModule` is a `TraversablePolynomialSourceModule` and a `PolynomialFailingModule`

{% include_code JavaOneUniversity2014_SuccessOrFailureModule lang:scala JavaOneUniversity2014_SuccessOrFailureModule.scala %}

StateModule
-----------

`StateModule` is a `StatefulSourceModule`

{% include_code JavaOneUniversity2014_StateModule lang:scala JavaOneUniversity2014_StateModule.scala %}

AsyncModule
-----------

`AsyncModule` is an `AsynchronousSourceModule`

{% include_code JavaOneUniversity2014_AsyncModule lang:scala JavaOneUniversity2014_AsyncModule.scala %}

`AsyncModule` makes use of the type `TimeOut`, the _trait_ `SimpleFuture` and the _class_ `Atomic`

{% include_code JavaOneUniversity2014_TimeOut lang:scala JavaOneUniversity2014_TimeOut.scala %}

{% include_code JavaOneUniversity2014_SimpleFuture lang:scala JavaOneUniversity2014_SimpleFuture.scala %}

{% include_code JavaOneUniversity2014_Atomic lang:scala JavaOneUniversity2014_Atomic.scala %}










