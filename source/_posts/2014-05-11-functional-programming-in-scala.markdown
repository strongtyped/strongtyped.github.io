---
layout: post
title: "Functional Programming in Scala"
date: 2014-05-11 06:21:00 +0200
author: Luc Duponcheel
comments: true
---

[home](/)

This blog post is the first one of a series of blog posts about _Functional Programming in Scala_.
It is also a teaser for series of blog posts of about _Reactive Programming in Scala_.

__Warning:__
Functional Programming and Reactive Programming are profound topics.
Hopefully, you will be rewarded for your effort to read the blog posts.

<!-- more -->

Binding
-------

You are, most probably, used to think of 

* _functions_ in terms of _applying_ them to _values_

but, of course, you can also think of

* _values_ in terms of _binding_ them to _functions_

The method `bind` below, defined in terms of `apply`, formalizes binding

``` scala
  implicit class AnyOps[Z](z: Z) {
    def bind[Y](z2y: Z => Y): Y =
      z2y apply z
  }
```

Strictly speaking it is not necessary at all to introduce binding in Scala,
but, sometimes, `bind` is more convenient to use than `apply`, as illustrated below

``` scala
scala> 1 bind { z => z + 1 } bind { z => z + 1 }
res0: Int = 3

scala> { (z: Int) => z + 1 } apply ({ (z: Int) => z + 1 } apply 1)
res1: Int = 3
```

* The method `bind` naturally associates to the _left_, while
the method `apply` associates to the _right_,
requiring the usage of _parantheses_.

* The Scala type inferencer naturally infers the type of expressions using `bind`, while
it does not infer the type of expressions using `apply`,
requiring the usage of _type annotations_.

Functional Programming
----------------------

Here is some quote about Functional Programming

_Functional Programming strictly separates_

* _the declarative description of computations_
* _the imperative execution of computations_

So, what are the computations in this quote all about?

Computations of type `One`
--------------------------

Let's get started with _pure computations_ that, when _executed_, result in _exactly one value_
as defined by the following `case class`

``` scala
  case class One[+Z](z: Z)
```

Computations resulting in exactly one value are also referred to as computations of type `One`.


When there is no danger of confusion, any kind of computation is simply referred to as a computation.

Constructing computations using `mkOne`
---------------------------------------

Computations of type `One` can be _constructed_ using `mkOne`

``` scala
  def mkOne[Z](z: Z) = One(z)
```

Agreed, introducing `mkOne` is somewhat of an overkill,
but it is consistent with the way other kinds of computations are constructed
in this series of posts.

How are computations of type `One` _described_?

Describing Computations of type `One`
-------------------------------------

Here is a computation of type `One` that is described using `mkOne`

``` scala
scala> lazy val oneFoo = mkOne("foo")
oneFoo: Fp.One[String] = <lazy>
```

A `lazy val` is used to separate the description of the computation from its execution.

How are computations of type `One` _executed_ once they are described?

Executing Computations of type `One`
------------------------------------

For the purpose of this series of blog posts,
executing a computation of type `One` can be as simple as _printing its value_.

``` scala
  def execute[Z](oz: One[Z]): Unit =
    oz match {
      case One(z) => println(z)
    }
```

Below, the computation of type `One` is executed using `execute`

``` scala
scala> execute(oneFoo)
foo
```

The computation `oneFoo` is defined as a `lazy val`.
`mkOne("foo")` is evaluated _the first time_ that `oneFoo` is executed.
When `oneFoo` would have been defined as a `def`, then
`mkOne("foo")` would be evaluated _every time_ that `oneFoo` is executed.
For `oneFoo` the difference does not matter
(apart from the fact that a `lazy val` is more time efficient and less space efficient than a `def`).
Sometimes the difference between a `lazy val` computation and a `def` computation _does_ matter.

Declaring simple computations like `oneFoo` and executing them as `execute(oneFoo)` is,
of course, not the end of the story about computations of type `One`.

Is there not a way to _compose_ computations of type `One`?

Composing Pure Computations of type `One`
-----------------------------------------

The implicit class below defines two methods, `bnd` and `and`, that can be used to
describe _more complex_ computations by, somehow, _composing_ them from _simpler_ ones.

``` scala
  implicit class OneOps[Z](oz: One[Z]) {
    def bnd[Y](z2oy: Z => One[Y]): One[Y] =
      oz.z bind z2oy
    def and[Y](o_z2y: One[Z => Y]): One[Y] =
      mkOne(oz.z bind o_z2y.z)
  }
```

Although, strictly speaking, not necessary, `bnd` and `and` are defined in terms of `bind`.

DSL for computations of type `One`
----------------------------------

By now we have defined `mkOne`, `bnd` and `and`

* They constitute a _declarative DSL for describing computations_ of type `One`

We have also defined `execute`

* It constitutes _an imperative DSL for executing computations_ of type `One`

This section contains some examples on how to make use of those DSL's.

All examples make use of the following `add` function on strings

```scala
  def add(z: String)(y: String) = z + y
```

`add` is a _curried_ version of the `+` operator on strings.

Let's start with a computation using `bnd`

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

  lazy val oneFooBar01: One[String] =
    mkOne("foo") bnd { z =>
      mkOne("bar") bnd { y =>
        mkOne(add(z)(y))
      }
    }

// Exiting paste mode, now interpreting.

oneFooBar01: Fp.One[String] = <lazy>

scala> execute(oneFooBar01)
foobar
```

The declaration of `oneFooBar01` can, informally, be explained as follows

* evaluate the expression `"foo"` and bind the resulting value to the variable `z`, and then
* evaluate the expression `"bar"` and bind the resulting value to the variable `y`, and then
* result in the value obtained by adding `z` and `y`

Let's continue with a computation using `and`

``` scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

  lazy val oneFooBar02: One[String] =
    mkOne("bar") and {
      mkOne("foo") and {
        mkOne(z => y => add(z)(y))
      }
    }

// Exiting paste mode, now interpreting.

oneFooBar02: Fp.One[String] = <lazy>

scala> execute(oneFooBar02)
foobar
```

The declaration of `oneFooBar02` can, informally, be explained as follows

* evaluate the expression `"bar"`, and evaluate the expression `"foo"`, and then
* bind the resulting values to the variables `y` and `z`, and then
* result in the value obtained by adding `z` and `y`

Let's finish with a simplified version of the computation using `and` above
in which there are no variables used any more

``` scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

  lazy val oneFooBar03: One[String] =
    mkOne("bar") and {
      mkOne("foo") and {
        mkOne(add)
      }
    }

// Exiting paste mode, now interpreting.

oneFooBar03: Fp.One[String] = <lazy>

scala> execute(oneFooBar03)
foobar
```

The declaration of `oneFooBar03` can, informally, be explained as follows

* evaluate the expression `"bar"`, and evaluate the expression `"foo"`, and then
* result in the value obtained by adding their resulting values

You may ask yourself: _this looks like much ado about nothing_.
We might as well have written the following code

``` scala
  val z = "foo"
  val z = "bar"
  add(z)(y)
```

Well, in a way you are right: for pure computations resulting in exactly one value,
introducing `mkOne`, `bnd` and `and` _is_ much ado about nothing.
For more interesting kinds of computations, the situation is completely different.

Nevertheless it is already possible to formulate some interesting remarks.

Some remarks
------------

First, a somewhat philosophical (and highly subjective) remark about _syntax_:

In _some_ way, the DSL based code reads more natural _from left to right_
than the language based code. When initializing `z` with `"foo"` using `val z = "foo"`,
_first_ the expression `"foo"` on the _right_ hand side of `=` is evaluated and
_second_ the variable `z` on the _left_ hand side of `=` is initialized with the resulting value.
Not very left to right indeed (in fact _right to left_ instead).
Agreed, in _another_ way, the DSL based code reads less natural because of all those curly braces.
Their nesting is usually limited since it is not a good programming practice to define
computations using large pieces of code (good programmers write small pieces code). 
The curly braces also have an advantage: they clearly delimit the scope of the variables that are introduced.
_Every disadvantage has its advantage_ (a quote of the Dutch soccer player Johan Cruijff).

Second, a remark about _semantics_.

The semantics of the language based code is defined by the _language specification_.
The semantics of the DSL based code is defined by _you_ (in terms of the semantics of `bnd` and `and`).
This provides you with more flexibility.
Agreed, again, in the case of pure computations resulting in exactly one value,
there is not really a lot of flexibility for you to define `bnd` and `and` in a meaningful way.
For more interesting kinds of computations, the situation is completely different.

About abstraction
-----------------

The method `bnd` is more _specific_ (_concrete_) than `and`
(the method `and` is more _general_ (_abstract_) than `bnd`).

The method `and` is more abstract than `bnd` simply because the method `and` can be defined in terms of `bnd`
(the definition of `and` in terms of `bnd` is referred to as the _default_ definition of `and`)

``` scala
    def and[Y](o_z2y: One[Z => Y]): One[Y] =
      oz bnd { z =>
        o_z2y bnd { z2y =>
          mkOne(z bind z2y)
        }
      }
```

For pure computations resulting in exactly one value, the two definitions are equivalent.
For more interesting kinds of computations,
`and` can be given a definition that is _not_ equivalent with the default definition.

About power of expression
-------------------------

The method `bnd` is _more powerful_ than `and`
(the method `and` is _less powerful_ than `bnd`).

Below is an example that illustrates that the method `bnd` is more powerful than `and`:
when using `bnd` the variable `z` is _immediately in scope_, and can be used in the rest of the code,
for example, to decide, based on `z`, how to proceed

``` scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

  def decideBasedOn(s: String): One[String] =
    mkOne(s) bnd { s =>
      s match {
        case "foo" => mkOne("bar") bnd { t =>
          mkOne(add(s)(t))
        }
        case "bar" => mkOne("foo") bnd { t =>
          mkOne(add(s)(t))
        }
        case _ => mkOne("bogus")
      }
    }

// Exiting paste mode, now interpreting.

decideBasedOn: (string: String)Fp.One[String]

scala> execute(decideBasedOn("foo"))
foobar

scala> execute(decideBasedOn("bar"))
barfoo

scala> execute(decideBasedOn(""))
bogus
```

The bottom line of all this is that, for most computations, the choice is yours

* you can go for less abstract, more powerful composition (and less implementation flexibility)
* you can go for more abstract, less powerful composition (and more implementation flexibility)

It is all about choosing the _right level of abstraction_.
As a programmer, you should _value abstraction_:
it is a tool to _manage complexity_.

Consider the difference between using `bnd` and `and`

* the description of `oneFooBar01` requires the usage of variables
* the description of `oneFooBar03` does not require the usage of variables

You may ask yourself: _are there any other ways to compose computations_?

Other ways to compose computations
----------------------------------

The answer is _yes_, there is one other popular way to compose computations

``` scala
  type ==>[-Z, +Y] = Z => One[Y]

  implicit class OneArrowOps[Z, Y](oa_z2y: Z ==> Y) {
    def >>>[X](oa_y2x: Y ==> X): Z ==> X =
      z => z bind oa_z2y bnd oa_y2x
  }
```

On the one hand, since `>>>` can be defined in terms of `bnd` (as shown above)
`>>>` is more abstract (and less powerful) than `bnd`.

On the other hand, since, in a way, `and` can be defined in terms of `>>>` (not shown in this post)
`and` is more abstract (and less powerful) than `>>>`.

The first series blog posts about functional programming
is not dealing with this third kind of computation composition.


Reactive Programming
--------------------

So far we worked with pure computations resulting in exactly one value.
But, of course, there are also more interesting kinds of computations.

_Reactive Programming_, deals with two important _computational features_: _failure_ and _latency_.
Both should be dealt with for realizing _responsiveness_.

`and` can be defined to deal with them in another way as `bnd`.


 


