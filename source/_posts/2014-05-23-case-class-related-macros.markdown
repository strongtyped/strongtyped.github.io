---
layout: post
title: "Macros: case class related macros"
date: 2014-05-23 09:00:00 +0200
author: Luc Duponcheel
comments: true
---

This is a first blog post about _Writing Macros in Scala_.
This blog post covers two interesting pairs of macros dealing with _case classes_.
The macros of the first pair translate _case classes to tuples_, and _tuples to case classes_.
The macros of the second pair translate _case classes to maps_, and _maps to case classes_.

__Remark:__
The macros presented in this blog can be found elsewhere on the internet.
We tried to reuse as much code as possible for both pairs of macros using a `Common` singleton object. Moreover, we end by adding a bit of extra code of our own as well. 

<!-- more -->


Case classes and Tuples
-----------------------

The tranformations
from case classes `Z` to tuples `T`
and
from tuples `T` to case classes `Z`
can be formalized as follows

``` scala
  trait ToTuple[Z, T] {
    def toTuple(z: Z): T
  }

  trait FromTuple[Z, T] {
    def fromTuple(t: T): Z
  }
```

The `toTuple` and `fromTuple` functions
---------------------------------------

It turns out that it is convenient to define _implicit macros_ of type `ToTuple[Z, T]` and `FromTuple[Z, T]`.
Assuming the existence of such macros, it is also convenient to define functions
`toTuple` and `fromTuple` that  `implicitly` make use of them

``` scala
  def toTuple[Z: ({ type ToTuple_[Z] = ToTuple[Z, T] })#ToTuple_, T](z: Z): T =
    implicitly[ToTuple[Z, T]].toTuple(z)
  def fromTuple[Z: ({ type FromTuple_[Z] = FromTuple[Z, T] })#FromTuple_, T](t: T): Z =
    implicitly[FromTuple[Z, T]].fromTuple(t)
```

The definitions above make use of type constructors `ToTuple_` resp. `FromTuple_`,
having _one type parameter_, that are obtained by _partially binding_ the
the type `T` to the second type parameter of the type constructors `ToTuple` resp. `FromTuple`,
having _two type parameters_.

An example case class
---------------------

Consider the case class 

* `case class Person(name: String, age: Int)`

The tuple class corresponding to this case class is

* `(String, Int)`

Here is the code that a macro should generate to convert a `Person` instance
to a `(String, Int)` instance

``` scala
    new ToTuple[Person, (String, Int)] {
      def toTuple(z: Person) = (z.name, z.age)
    }
```

Here is the code that a macro should generate to convert a `(String, Int)` instance
to a `Person` instance

``` scala
    new FromTuple[Person, (String, Int)] {
      def fromTuple(t: (String, Int)) = Person(t._1, t._2)
    }
```

So, what is the information that we require at compile time
for being able to generate this code?

* we require the string `"Person"`
* we require the strings `"String"` and `"Int"`
* we require the strings `"name"` and `"age"`
* we require the number `2` to generate the strings `"1"` and `"2"`

The last requirement is not really needed if we replace the first three
requirements above by

* we require the string `"Person"`
* we require the list of strings containing `"String"` and `"Int"`
* we require the list of strings containing `"name"` and `"age"` 

because the lists above have size `2`.

So how can we obtain all those required strings?

The `getFieldNamesAndTypes` method
----------------------------------

The following code is all we need to obtain the required strings

``` scala
  trait Common {
    def getFieldNamesAndTypes(c: Context)(tpe: c.universe.Type):
      Iterable[(c.universe.Name, c.universe.Type)] = {
      import c.universe._

      object CaseField {
        def unapply(trmSym: TermSymbol): Option[(Name, Type)] = {
          if (trmSym.isVal && trmSym.isCaseAccessor)
             Some((newTermName(trmSym.name.toString.trim), trmSym.typeSignature))
          else
            None
        }
      }

      tpe.declarations.collect {
        case CaseField(nme, tpe) =>
          (nme, tpe)
      }
    }
  }
```

The code above has a `Context` parameter `c`, using which we can obtain all
the types we need using `import c.universe._`. 

The only information available to start generating code from is `tpe`, having type `Type`.
The `object CaseField`'s `unapply` method can be used to_ extract_ the
information that we need from the _declarations_ of `tpe`,
using a _collection_ that uses an appropriate `CaseField` pattern matching partial function.

Maybe, by looking at the code, you may wonder why it uses
the somewhat complex expression `newTermName(trmSym.name.toString.trim)` instead
of the much simpler expression `trmSym.name`.
Writing macros is still somewhat of a _black art_.
It turns out that using `trmSym.name` would result in strings
`"name "` and `"age "`, with an extra blank at the end,
and we can get rid of that extra blank using
the more complex expression `newTermName(trmSym.name.toString.trim)`.

The `toTupleMacro` macro
------------------------

Using the `getFieldNamesAndTypes` method we can now define the
implicit `toTupleMacro` as follows
  
``` scala
  object ToTuple extends Common {
    implicit def toTupleMacro[Z, T]: ToTuple[Z, T] = macro toTupleMacroImpl[Z, T]

    def toTupleMacroImpl[Z: c.WeakTypeTag, T](c: Context): c.Expr[ToTuple[Z, T]] = {
      import c.universe._

      val tpe: Type = weakTypeOf[Z]

      val (nmes, tpes) = getFieldNamesAndTypes(c)(tpe).unzip

      val fldSels: Iterable[Select] = nmes.map { nme =>
        q"""
         z.$nme
        """
      }

      val toTuple: Tree =
        q"""
         new ToTuple[$tpe, (..$tpes)] {
           def toTuple(z: $tpe) = (..$fldSels)
         }
        """

      c.Expr[ToTuple[Z, T]](toTuple)
    }
  }
```

The macro, in this case one of type `ToTuple[Z, T]`, is _declared_ using a `macro` keyword.
The macro is _definied_ using a method of type `c.Expr`,
in this case of type `c.Expr[ToTuple[Z, T]]`.
The method has an extra parameter of type `Context`.
The macro makes use of _quasi-quotes_, the preferred way to generate code. We use `unzip`
to turn a list of tuples into a tuple of lists. The `tpes` can be used as such in the
quasi-quote expression that defines `toTuple`. The `nmes` can be used to define _field selections_
`fldSels` that can be used in the quasi-quote expression that defines `toTuple`.
The variable `toTuple` has type `Tree` and the macro needs a `c.Expr[ToTuple[Z, T]]`
type. Therefore we wrap `toTuple` into an expression of type `c.Expr[ToTuple[Z, T]]`.


The `fromTupleMacro` macro
------------------------

Using the `getFieldNamesAndTypes` method we can now define the
implicit `fromTupleMacro` as follows
  
``` scala
  object FromTuple extends Common {
    implicit def fromTupleMacro[Z, T]: FromTuple[Z, T] = macro fromTupleMacroImpl[Z, T]

    def fromTupleMacroImpl[Z: c.WeakTypeTag, T](c: Context): c.Expr[FromTuple[Z, T]] = {
      import c.universe._

      val tpe: Type = weakTypeOf[Z]

      val (nmes, tpes) = getFieldNamesAndTypes(c)(tpe).unzip
      
      def prj(i: Int): TermName = newTermName("_" + i)

      val prjs: Seq[Select] = (1 to nmes.toSeq.size).map { i =>
        q"""
         t.${prj(i)}
        """
      }

      val tpeSym: Symbol = tpe.typeSymbol.companionSymbol

      val fromTuple: Tree =
        q"""
         new FromTuple[$tpe, (..$tpes)] {
           def fromTuple(t: (..$tpes)) = ${tpeSym}(..$prjs)
         }
        """

      c.Expr[FromTuple[Z, T]](fromTuple)
    }
  }
```

The macro, in this case one of type `FromTuple[Z, T]`,  is _declared_ using a `macro` keyword.
The macro is _definied_ using a method of type `c.Expr`,
in this case of type `c.Expr[FromTuple[Z, T]]`.
The method has an extra parameter of type `Context`.
The macro makes use of _quasi-quotes_, the preferred way to generate code. We use `unzip`
to turn a list of tuples into a tuple of lists. The `tpes` can be used as such in the
quasi-quote expression that defines `fromTuple`. The `nmes` can be used to define _projections_
`prjs` that can be used in the quasi-quote expression that defines `fromTuple`.
We also need `tpeSym` the _type symbol_ of the _companion object_ of the case class.
The variable `fromTuple` has type `Tree` and the macro needs a `c.Expr[FromTuple[Z, T]]`
type. Therefore we wrap `fromTuple` into an expression of type `c.Expr[FromTuple[Z, T]]`.

Here is an example

``` scala
object MacroApp extends App {
  case class Person(name: String, age: Int) 
    
  val person = new Person("John", 40)
  
  println(toTuple(person))
  println(fromTuple[Person, (String, Int)](("John", 40)))
}
```

printing

``` scala
(John,40)
Person(John,40)
```

Case classes and Maps
---------------------

The tranformations
from case classes `Z` to maps `Map[String, Any]`
and
from maps `Map[String, Any]` to case classes `Z`
can be formalized as follows

``` scala
  type M = Map[String, Any]
 
  trait Store[Z] {
    def store(z: Z): M
  }

  trait Load[Z] {
    def load(m: M): Z
  }
```

The `store` and `load` functions
---------------------------------------

It turns out that it is convenient to define _implicit macros_ of type `Store[Z]` and `Load[Z]`.
Assuming the existence of such macros, it is also convenient to define functions
`store` and `load` that `implicitly` make use of them 

``` scala
  def store[Z: Store](z: Z): M =
    implicitly[Store[Z]].store(z)
  def load[Z: Load](m: M): Z =
    implicitly[Load[Z]].load(m)
```

An example case class
---------------------

Consider the case class 

* `case class Person(name: String, age: Int)`

Here is the code that a macro should generate to convert a `Person` instance
to an `M` instance

``` scala
  new Store[Person] {
    def store(z: Person): M =
      Map("name" -> z.name, "age" -> z.age)
  }
```

Here is the code that a macro should generate to convert an `M` instance
to a `Person` instance

``` scala
  new Load[Person] {
    def load(m: M): Person =
      Person(m("name").asInstanceOf[String], m("age").asInstanceOf[Int])
  }
```

The information that we require at compile time
for being able to generate this code is exactly the same
as when converting from and to tuples, so we can reuse
the `getFieldNamesAndTypes` method.

The `storeMacro` macro
----------------------

Using the `getFieldNamesAndTypes` method we can now define the
implicit `storeMacro` as follows

``` scala
  object Store extends Common {
    implicit def storeMacro[Z]: Store[Z] = macro storeMacroImpl[Z]

    def storeMacroImpl[Z: c.WeakTypeTag](c: Context): c.Expr[Store[Z]] = {
      import c.universe._

      val tpe: Type = weakTypeOf[Z]

      val mpgs: Iterable[Apply] =
        getFieldNamesAndTypes(c)(tpe).map {
          case (nme, tpe) =>
            q"""
            ${nme.decoded} -> z.${nme}
            """
        }

      val store: Tree =
        q"""
         new Store[$tpe] {
           def store(z: $tpe): M =
             Map(..$mpgs)
         }
         """

      c.Expr[Store[Z]](store)
    }
  }
```

The macro, in this case one of type `Store[Z]`, is _declared_ using a `macro` keyword.
The macro is _definied_ using a method of type `c.Expr`,
in this case of type `c.Expr[Store[Z]]`.
The method has an extra parameter of type `Context`.
The macro makes use of _quasi-quotes_, the preferred way to generate code. 
`getFieldNamesAndTypes(c)(tpe)` can be used to define _mappings_
`mpgs` that can be used in the quasi-quote expression that defines `store`.
The mappings use the field names in two ways: with and without double-quotes.
The variable `store` has type `Tree` and the macro needs a `c.Expr[Store[Z]]`
type. Therefore we wrap `store` into an expression of type `c.Expr[Store[Z]]`.

The `loadMacro` macro
----------------------

Using the `getFieldNamesAndTypes` method we can now define the
implicit `loadMacro` as follows

``` scala
  object Load extends Common {
    implicit def loadMacro[Z]: Load[Z] = macro loadMacroImpl[Z]

    def loadMacroImpl[Z: c.WeakTypeTag](
      c: Context): c.Expr[Load[Z]] = {
      import c.universe._

      val tpe: Type = weakTypeOf[Z]

      val args: Iterable[Tree] =
        getFieldNamesAndTypes(c)(tpe).map {
          case (nme, tpe) =>
            q"""
            m(${nme.decoded}).asInstanceOf[${tpe}]
            """
        }

      val tpeSym: Symbol = tpe.typeSymbol.companionSymbol

      val load: Tree =
        q"""
         new Load[$tpe] {
           def load(m: M): $tpe =
             ${tpeSym}(..$args)
         }
         """

      c.Expr[Load[Z]](load)
    }
  }
```

The macro, in this case one of type `Load[Z]`, is _declared_ using a `macro` keyword.
The macro is _definied_ using a method of type `c.Expr`,
in this case of type `c.Expr[Load[Z]]`.
The method has an extra parameter of type `Context`.
The macro makes use of _quasi-quotes_, the preferred way to generate code.
`getFieldNamesAndTypes(c)(tpe)` can be used to define _case class constructor arguments_
`args` that can be used in the quasi-quote expression that defines `load`.
We also need `tpeSym` the _type symbol_ of the _companion object_ of the case class.
The variable `load` has type `Tree` and the macro needs a `c.Expr[Load[Z]]`
type. Therefore we wrap `load` into an expression of type `c.Expr[Load[Z]]`. 

Here is an example

``` scala
object MacroApp extends App {
  case class Person(name: String, age: Int) 
    
  val person = new Person("John", 40)
  
  println(store(person))
  println(load[Person, (String, Int)](Map("name" -> "John", "age" -> 40)))
}
```

printing

``` scala
Map(name -> John, age -> 40)
Person(John,40)
```

Loading and Storing other types
-------------------------------

Now that we know how to load and store case class instances, what about other classes?
If there exist _bidirectional mappings_ from a class to a case class, then we can use
those mappings to load and store instances of that class.
The way those bidirectional mappings can be used is formalized as follows

``` scala
  def lift[Z, Y](z2y: Z => Y): Load[Z] => Load[Y] =
    lz => new Load[Y]() {
      def load(m: M) = z2y(lz.load(m))
    }

  def tfil[Z, Y](z2y: Z => Y): Store[Y] => Store[Z] =
    sy => new Store[Z]() {
      def store(s: Z) = sy.store(z2y(s))
    }

  def lift2load[Z: Load, Y](z2y: Z => Y): Load[Y] =
    lift(z2y)(implicitly[Load[Z]])

  def tfil2store[Z, Y: Store](z2y: Z => Y): Store[Z] =
    tfil(z2y)(implicitly[Store[Y]])
```

We have called the first function `lift` and the second one `tfil` to emphasize that
`lift` is _covariant_ (the arrow direction is preserved) and
`tfil` _contravariant_ (the arrow direction has changed).

Here is a first example

``` scala
  class Customer(val named: String, val aged: Int)

  def person2customer(person: Person) = new Customer(person.name, person.age)
  def customer2person(customer: Customer) = Person(customer.named, customer.aged)

  println(lift2load(person2customer).load(Map("name" -> "John", "age" -> 40)))
  println(tfil2store(customer2person).store(new Customer("John", 40)))
```

printing

``` scala
Customer(John,40)
Map(name -> John, age -> 40)
```

Here is a second example,
making use of the bidirectional mappings from case classes to tuples

``` scala
  println(lift2load(toTuple[Person, (String, Int)]).load(Map("name" -> "John", "age" -> 40)))
  println(tfil2store(fromTuple[Person, (String, Int)]).store(("John", 40)))
```

printing

``` scala
(John,40)
Map(name -> John, age -> 40)
```









 




