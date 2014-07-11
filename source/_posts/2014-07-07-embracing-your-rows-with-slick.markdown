---
layout: post
title: "Embracing your rows with Slick"
date: 2014-07-07	 09:00:00 +0200
author: Renato Cavalcanti
comments: true
---

This post is a follow-up of my talk "Slick in the field, learning to forget ORM" presented at ScalaDays 2014. The video is available on [Parleys](http://www.parleys.com/play/53a7d2c6e4b0543940d9e54c) and can be watched on the bottom of this page.

At Scala Days I presented the project [ActiveSlick](https://github.com/strongtyped/active-slick) which is a thin layer on top of Slick (v.20) providing some abstractions to ease object lifecycle management and the implementation of a type-safe ActiveRecord pattern. 

Slick is wonderful piece of code that allows you to build and compose sql queries using plain Scala code. The advantages of that are many, but the most important, in my opinion, is the high re-utilisation of queries by composition and the compile time checking offered by the Scala compiler.

This post is NOT an introduction to Slick, neither was my talk at Scala Days. The goal is to explain how I've solved part of the object lifecycle management and my vision about how one should model applications using Slick.

<!-- more -->
# Object lifecycle management

It's very common nowadays to find articles blaming ORM frameworks and stating that ORM is an anti-pattern. I won't go into that discussion. I don't think that ORM frameworks offer a perfect solution, but much like everything in our industry, there is no perfect solutions. There is always some trade-offs to make when choosing one or other tool.

Martin Fowler once wrote an article called "[OrmHate](http://martinfowler.com/bliki/OrmHate.html)" where he discuss this ORM blaming trend. The main argument against ORM is that it tries to workaound the inherent problem of connecting an object oriented model to relational database tables. However, Martin's point of view is that ORM frameworks are not only trying to solve the impedeance mischmatch problem, they are also helping us to deal with two representations of our data. One representation being the in-memory model expressed in an object oriented fashion and another representation in the form of relational database tables. 

These two representations of our data must be kept in sync. And this is true for any persistency library. Being that an ORM framework, Slick, pure JDBC, but also any NoSql storage.  

# Embracing your rows





<iframe type="text/html" width="100%" height="410" mozallowfullscreen="true" webkitallowfullscreen="true" src="http://www.parleys.com/share.html#play/53a7d2c6e4b0543940d9e54c" frameborder="0">&lt;br /&gt;</iframe>