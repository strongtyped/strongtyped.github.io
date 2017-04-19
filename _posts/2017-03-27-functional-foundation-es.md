---
layout: post
title: "A Functional Foundation for CQRS/ES in Scala"
date: 2017-03-27
description: 
categories: 
  - scala
  - FP
  - Event Sourcing
  - CQRS
image: https://source.unsplash.com/Q6NCx-h7KpY/2000x300?a=.png
author_staff_member: renato
comments: true
---
I've been practicing CQRS and Event Sourcing for quite some time now. Since the begining I have been trying to elaborate what are the basic functions that every Event Sourced applications must support. It turns out that there is only one single function, but for practical reasons it's preferable to split it in at least two other functions, as we will see soon. 

But before we start I would like to mention another blog post from Mathias Verraes entitled [A Functional Foundation for CQRS/ES](http://verraes.net/2014/05/functional-foundation-for-cqrs-event-sourcing/). I discovered his blog not so long ago and wasn't surprised to see the similarities to what I was doing in [Fun.CQRS](http://www.funcqrs.io). This is basically because CQRS and Event Sourcing is quite simple and it doesn't take much to identify what are the basic operations (or functions) needed to support it. However, and that's the main motivation of this blog, one must make some decisions when building a CQRS framework in order to cut the abstractions at the right place. 

That said, this blog will explain which functions we are looking for, how we have cut them in smaller functions and how they will look like in Scala. Also, we will only focus on the command side of CQRS. We will dedicate another blog for the query side.

<!-- more -->

## Command Side

In it's most simple form the command side of CQRS application can be defined as a model to which we can send `Commands` that are validated and if accepted may emit one or more `Events`. The model is often called an `Aggregate` which is a term borrowed from DDD.

An `Event` expresses a mutation to the `Aggregate` itself and once emitted it's applied to the `Aggregate` generating an updated version of it. 

We can already spot two basic functions. One that validates the `Command` and one that applies the `Event` to the `Aggregate`. 

We could defined them as follow...
```scala
type Events = Seq[Event]
(Aggregate, Command) => Events         // validate and emits events
(Aggregate, Event) => Aggregate        // apply and updates Aggregate state

// from which we can derive, where the returned Aggregate is an updated version
(Aggregate, Command) => (Aggregate, Events)
``` 
The first function is usually called a `Command Handler` as it handles an incoming `Command`. The second is commonly refered as an `Event Handler`. 

These functions are not yet what we want. There are a few missing bits. As we said, the `Command Handler` will validate the command and decide if `Events` should be emitted. One possible way of doing it is returning a empty list of `Events`, but that is not desirable. We want to inform the caller why the command was rejected. Therefore we need to wrap `Events` on a type that can express success or failure. 

Our command handler will therefore look like....
```scala
type Events = Seq[Event]
(Aggregate, Command) => F[Events]

// from which we can derive
(Aggregate, Command) => F[(Aggregate, Events)]
``` 
Note that the return type is encoded as `F`, thus not yet defined. If can be any type that can be used to express an error condition, it could be a `Future`, a `Try`, an `Option` (although not ideal) or an applicative like `cats.Validated` or `scalaz.Validation`. In which case proper aliasing would be required to comply with a type constructor with one parameter. How to do it is out-of-scope of this blog, so we will simply continue using `F` and considering that it's a type that can express failure as well. We will later come back to it an explain why we keep it as `F`.

## In the begining there is None
It turns out that we don't have an `Aggregate` at the beginning. At some point an initial shot must be given to create the `Aggregate`. So, instead of modeling our functions as above, we should have:
```scala
type Events = Seq[Event]
(Option[Aggregate], Command) => F[Events]
(Option[Aggregate], Event) => Aggregate

// from which we can derive
(Option[Aggregate], Command) => F[(Aggregate, Events)]
```
Whenever we have a `None` as value for `Option[Aggregate]`, we say that we are handling a **Seed Command** or a **Seed Event**. Basically, given a `None` and a `Command` we must decide if the conditions are met to emit the first `Event` that will effectivelly initialise the `Aggregate`. 

## An Aggregate aggregates its past
The shape of the **Event Handler** function reveals that an `Aggregate` is an accumulation of its past events. We start with a `None` and a **Seed Event**, we generate the first `Aggregate`'s instance and subsequentially we can update it by applying the next events to it, one by one.

Therefore we can consider that `Aggregate == History`, where `History` is the sequence of all events that took place since the **Seed Event** until now.

It's also true that if `History` is a `Seq[Event]`, than we could define our functions as:
 ```scala
type History = Seq[Event]
(History, Command) => F[History]
(History, Event) => History

// from which we can derive, 
type NewEvents = Seq[Event]
(History, Command) => F[(Aggregate, NewEvents)] // updated Aggregate + new Events
(History, Command) => F[(History, NewEvents)] // augmented history + new Events

// and finally
(History, Command) => F[History] // return new agumented history
```  
There are a few interesting consequences when we express it on those terms.   

Firstly, we can completely remove the `Option[Aggregate]`. As a matter of fact an empty `History` is the equivalent of an undefined `Option[Aggregate]`, ie: `None`.

Secondly, the final funciton shows that we can replace `(Aggregate, Events)` by  `History`.

At that point we can affirm that the most fundamental function on a the command side of CQRS is:
```scala 
(History, Command) => F[History]  
```
It boils down to "given a previous `History` (may be empty) and a new `Command`, a new augmented `History` is produced or an error is returned.

It also true that all the **event handling**, ie: **hisory** aggregation, can be abstracted away.

## ... but that's not so practical

