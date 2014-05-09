---
layout: post
title: "Functional Programming in Scala"
date: 2014-04-28 23:25:34 +0200
comments: true
categories: advanced
author: Luc Duponcheel
published: false
---


This the first post of a series of posts about Functional Programming in Scala.

The post is also a teaser for the following posts on Reactive Programming in Scala.

Warning: the content of the posts on Functional Programmng is not trivial, but,
hopefully, you will be rewarded for your effort to read the posts.

Functional Programming
----------------------

Here is one of my favorite quotes about Functional Programming: 

Functional Programming strictly separates
the declarative description of computations
from the imperative execution of computations.

Binding
-------

Most of you are used to think of functions in terms of applying them to values.
But you can, of course, also think of values in terms of binding them to functions.

The method `bind` of the implicit class below formalizes binding

``` scala
implicit class AnyOps[Z](z: Z) {
  def bind[Y](z2y: Z => Y): Y =
    z2y apply z
}
```

Strictly speaking it is not necessary at all to introduce binding in Scala.
In my opinion, `bind` is sometimes more convenient to use as `apply`
as illustrated below

``` scala
scala> 1 bind { x => x + 1 } bind { x => x + 1 }
res0: Int = 3

scala> { x => x + 1 } apply { x => x + 1 } apply 1
<console>:11: error: missing parameter type
              { x => x + 1 } apply { x => x + 1 } apply 1
                ^

scala> { x => x + 1 } apply ({ x => x + 1 } apply 1)
<console>:11: error: missing parameter type
              { x => x + 1 } apply ({ x => x + 1 } apply 1)
                ^

scala> { (x: Int) => x + 1 } apply ({ (x: Int) => x + 1 } apply 1)
res3: Int = 3
```

Pure Computations resulting in exactly one value
------------------------------------------------

Let's get started with pure computations resulting in exactly one value
as defined by the following case class

``` scala
case class One[Z](z: Z) {
  def execute() = z
}
```

In this section computations resulting in exactly one value are
sometimes also simply referred to as computations for short.

The following `lazy val oneFoo`,
is a computation that is described in a declarative way as `One("foo")`

``` scala
scala> lazy val oneFoo = One("foo")
oneFoo: Fp.One[String] = <lazy>
```

Since there is nothing special about pure computations resulting in exactly one value,
executing `oneFoo` is as straightforward as evaluating `oneFoo.execute()`.

``` scala
scala> oneFoo.execute()
res0: String = foo
```

Composing Pure Computations resulting in exactly one value
----------------------------------------------------------

The implicit class below defines two methods `bnd` and `and` that can be used to compose computations.

``` scala
implicit class OneOps[Z](mz: One[Z]) {
  def bnd[Y](z2my: Z => One[Y]): One[Y] =
    mz.z bind z2my
  def and[Y](m_z2y: One[Z => Y]): One[Y] =
    One(mz.z bind m_z2y.z)
}
```

Note that, although strictly speaking not necessary, `bnd` and `and` are defined in terms of `bind`.

`One`, `bnd` and `and` define a DSL using which it is possible to describe computations in a declarative way.

All computations below make use of the following curried `add` function

```scala
def add(z: String)(y: String) = z + y
```

Let's start with a computation using `bnd`

```scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

  lazy val fooBar01: One[String] =
    One("foo") bnd { z =>
      One("bar") bnd { y =>
        One(add(z)(y))
      }
    }

// Exiting paste mode, now interpreting.

fooBar01: Fp.One[String] = <lazy>

scala> fooBar01.execute()
res0: String = foobar

```

The meaning of `fooBar01` can be explained as follows

* evaluate the expression `"foo"` and `bind` the resulting value to the variable `z`, and then
* evaluate the expression `"bar"` and `bind` the resulting value to the variable `y`, and then
* return the value obtained by adding `z` and `y`

Let's continue with a computation using `and`

``` scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

  lazy val fooBar02: One[String] =
    One("bar") and {
      One("foo") and {
        One(z => y => add(z)(y))
      }
    }

// Exiting paste mode, now interpreting.

fooBar02: Fp.One[String] = <lazy>

scala> fooBar02.execute()
res1: String = foobar
```

The meaning of `fooBar02` can be explained as follows

* evaluate the expression `"bar"`, and
* evaluate the expression `"foo"`, and then
* `bind` the resulting values to the variables `y` and `z`, and then
* return the value obtained by adding `z` and `y`

Let's finish with a simplified version of the computation using `and` above
(note: there are no variables used any more)

``` scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

  lazy val fooBar03: One[String] =
    One("bar") and {
      One("foo") and {
        One(add)
      }
    }

// Exiting paste mode, now interpreting.

fooBar03: Fp.One[String] = <lazy>

scala> fooBar03.execute()
res2: String = foobar
```

The meaning of `fooBar02` can be explained as follows

* evaluate the expression `"bar"`, and
* evaluate the expression `"foo"`, and then
* return the value obtained by adding the resulting values

You may ask yourself: this looks like much ado about nothing.
We might as well have written the following code
``` scala
val z = "foo"
val z = "bar"
add(z)(y)
```

Well, in a way, for pure computations resulting in exactly one value,
it is really much ado about nothing (for more interesting kind of
computations, the situation will be different). Nevertheless it
is already possible to formulate some remarks.

Here is a, somewhat philosophical (and highly subjective), remark about syntax:

In my opinion, somehow, the DSL based code reads more natural from left to right
than the language based code. Indeed, note that, in `val z = "foo"`,
first the expression `"foo"` on the right hand side of `=` is evaluated and
second the variable `z` on the left hand side of `=` is initialized with the resulting value.
Not very left to right indeed (in fact right to left instead).
Agreed, somehow the library based code reads less natural because of all those
curly braces `{` and `}`, but, typically, it is not a good programming practice
to write large code excerpts anyway, so their nesting is usually limited.

Here is a remark about semantics.

The semantics of the language based code is defined by the language specification
(implemented by the compiler).
The semantics of `fooBar01`, `fooBar02` and `fooBar03` is defined
in terms of the semantics of `bnd` and `and`. This provides you with more flexibility.
Agreed, again, in the case of pure computations resulting in exactly one value there
is not really a lot of choice for you to define `bnd` and `and` in a way that makes sense
(for more interesting kind of computations, the situation will be different).

The method `bnd` is more specific (concrete) and more powerful than `and`.
In other words: the method `and` is more general (abstract) and less powerful than `bnd`.

When using `bnd` the variable `z` is immediately in scope and can be used in the rest of the code,
for example, to decide how to proceed, as illustrated by the example below

``` scala
scala> :paste
// Entering paste mode (ctrl-D to finish)

  def mkFooBarUsing(string: String): One[String] =
    One(string) bnd { z =>
      z match {
        case "foo" => One("bar") bnd { y =>
          One(add(z)(y))
        }
        case "bar" => One("foo") bnd { y =>
          One(add(y)(z))
        }
        case _ => One("foobar") bnd { y =>
          One(y)
        }
      }
    }

// Exiting paste mode, now interpreting.

mkFooBarUsing: (string: String)Fp.One[String]

scala> mkFooBarUsing("foo").execute()
res0: String = foobar

scala> mkFooBarUsing("bar").execute()
res1: String = foobar

scala> mkFooBarUsing("").execute()
res2: String = foobar

```

Note that the method `and` could have been defined in terms of the more powerful method `bnd` as follows

``` scala
def and[Y](m_z2y: One[Z => Y]): One[Y] =
  mz bnd { z =>
    m_z2y bnd { z2y =>
      One(z bind z2y)
    }
  }
```

The two definitions are equivalent.
The situation will be different when dealing with reactive programming
where failure and latency (two important computational features) come into play,

 


