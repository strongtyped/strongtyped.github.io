---
layout: post
title: "Embracing your rows with Slick"
date: 2014-07-18 09:00:00 +0200
author: Renato Cavalcanti
comments: true
---

This post is a follow-up of my talk "*Slick in the field, learning to forget ORM*" presented at ScalaDays 2014. The video is available on [Parleys](http://www.parleys.com/play/53a7d2c6e4b0543940d9e54c) and can be watched on the bottom of this page.

At Scala Days I presented the project [ActiveSlick](https://github.com/strongtyped/active-slick) which is a thin layer on top of Slick (v.20) providing some abstractions to ease object lifecycle management and the implementation of a type-safe `ActiveRecord` pattern.

Slick is wonderful piece of code that allows you to build and compose sql queries using plain Scala code. The advantages of that are many, but the most important, in my opinion, is the high re-utilisation of queries by composition and the compile time checking offered by the Scala compiler.

This post is NOT an introduction to Slick, neither was my talk at Scala Days. The goal is to explain how I've solved part of the object lifecycle management and my vision about how one should model applications using Slick.

<!-- more -->
# Object lifecycle management

It's very common nowadays to find articles blaming ORM frameworks and stating that ORM is an anti-pattern. I won't go into that discussion. I don't think that ORM frameworks offer a perfect solution, but much like everything in our industry, there is no perfect solutions. There is always some trade-offs to make when choosing one or other tool.

Martin Fowler once wrote an article called [OrmHate](http://martinfowler.com/bliki/OrmHate.html) where he discuss this ORM blaming trend. The main argument against ORM is that it tries to workaround the inherent problem of connecting an object-oriented model to relational database tables. However, Martin's point of view is that ORM frameworks are not only trying to solve the impedance mismatch problem, they are also helping us to deal with two representations of our data. One representation being the in-memory model expressed in an object-oriented fashion and another representation stored in relational database tables. 

These two representations of data must be kept in sync. This is a challenge that any application doing persistence needs to solve. As soon you serialise/deserialise data to and from any kind of storage you have to make sure you synchronise them correctly.

The most common problems related to the object lifecycle are *identity* and *versioning*. Once we have *identity* and *versioning* we can answer the following questions: 

* Is this object already persisted? 
* Is this object dirty? Does it need to be saved back into storage? 
* Is this object stale? 

These are the typical lifecycle questions that we have for a single object. But this is not all. We also need to deal with collections of records. We must be able to preserve the ordering of elements and to know when an object was removed from a collection or added to it.

Contrary to other persistence libraries, Slick does not provide these facilities. To some extend this is expected as Slick is a database access library not a persistence framework. As stated in the Slick website, "*Slick is a modern database query and access library for Scala*". 

The question is how can we build a framework to solve the above mentioned problems on top of Slick? How can we build a framework that helps us to deal with single object and collection lifecycle using a type-safe database query and access library?


# Embrace your rows

Another import point of my talk was around the question: "*How to model your domain when using Slick?*".  

A very common question on the Slick mailing list is how to model one-to-many and many-to-many relations with Slick? After years of object-oriented and ORM practice it's natural that we have to spent some time to learn how to think differently. By consequence, we'll also have to learn how to model differently. 

My take on that is that we should code and model to our rows. When using Scala we often model using immutable data structures, i.e.: case classes.  Slick of course just builds on top of this paradigm. A case class model is a mirror of a table definition and an instance of such case class is just a row that was brought into memory or a row that still need to be inserted in a table.

**Embrace your rows** is a call-out to adopt this way of modelling. We often say that functions are first-class citizens in functional programming. That failure is a first-class citizen in reactive programming. Well, when using Slick, rows are first-class citizens. 

We could try to fight the ORM battle once again and build a ORM framework more suited to immutable data structures, with built-in nullable <-> Option conversion, with type-safe queries and query composition. But we can also model differently by keeping our in-memory model as close as possible to its relational database representation. And that's exactly what Slick does. 

Therefore the [ActiveSlick](https://github.com/strongtyped/active-slick) project goal is to build a thin layer on top of Slick to solve some basic object lifecycle challenges and to promote the idea of modelling with rows, much like when using the `ActiveRecord` pattern. Hence the name `ActiveSlick`. 

  
  
  
---
## Parleys Video: Slick in the field, learning to forget ORM
<iframe type="text/html" width="100%" height="410" mozallowfullscreen="true" webkitallowfullscreen="true" src="http://www.parleys.com/share.html#play/53a7d2c6e4b0543940d9e54c" frameborder="0"></iframe>
