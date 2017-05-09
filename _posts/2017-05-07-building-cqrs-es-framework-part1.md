---
layout: post
title: "Building a CQRS / ES Framework (part 1)"
date: 2017-05-07
description: 
categories: 
  - Scala
  - FP
  - Event Sourcing
  - CQRS
image: https://source.unsplash.com/daYpIvggFxA/2000x300?a=.png
author_staff_member: renato
comments: true
---

This is the first of a serie of posts where I will be covering **CQRS**, **Event Sourcing** and some **Functional Programming**. In this post we will concentrate on the functional foundation of **CQRS** and **Event Sourcing** in which I will explain the basic functions that supports the command side of **CQRS**. 

But before we start I would like to mention another blog post from Mathias Verraes entitled [A Functional Foundation for CQRS/ES](http://verraes.net/2014/05/functional-foundation-for-cqrs-event-sourcing/). I discovered his blog not so long ago and wasn't surprised to see the similarities to what I was doing in [Fun.CQRS](https://github.com/strongtyped/fun-cqrs). This because **CQRS** and **Event Sourcing** is quite simple and it doesn't take much to identify what are the basic operations (or functions) needed to support it. However, and that's the main motivation of this blog, one must make some decisions when building a **CQRS** framework in order to cut the abstractions at the right place. My intention is therefore to explain how I have used and defined those same functions to build [Fun.CQRS](https://github.com/strongtyped/fun-cqrs).

That said, this blog will explain which function we are looking for, how we can cut them in smaller functions and how they will look like in Scala. As already said, we will only focus on the command side of **CQRS**. We will dedicate another post for the query side.

<!-- more -->

## Command Side

In it's most simple form the command side of **CQRS** application can be defined as a model to which we can send **Commands** that are validated and if accepted may emit one or more **Events**. The model is often called an **Aggregate** which is a term borrowed from **DDD, Domain Driven Design**.

An **Event** expresses a mutation to the **Aggregate** itself and once emitted it's applied to the **Aggregate** generating an updated version of it. 

We can already spot two basic functions. One that validates the **Command** and one that applies the **Event** to the **Aggregate**. 

We could defined them as follow...
```scala
type Events = Seq[Event]

Aggregate => Command => Events   // validate and emits events
Aggregate => Event => Aggregate    // apply and updates Aggregate state

// from which we can derive 
Aggregate => Command => (Aggregate, Events) 
``` 
The first function is usually called a **Command Handler** as it handles an incoming **Command**. The second is commonly refered as an **Event Handler**. 

These functions are not yet what we want. There are a few missing bits. As we said, the **Command Handler** will validate the command and decide if **Events** should be emitted. One possible way of doing it is to return an empty list of **Events**, but that is not desirable. We want to inform the caller why the command was rejected. Therefore we need to wrap **Events** on a type that can express success or failure. 

The command handler will therefore look like....
```scala
type Events = Seq[Event]

Aggregate => Command => F[Events]

// from which we can derive
Aggregate => Command => F[(Aggregate, Events)]
``` 
Note that the return type is encoded as `F`, thus not yet defined. It can be any type that can express an error condition, it could be a `Future`, a `Try`, an `Either` or an applicative like `cats.Validated` or `scalaz.Validation`. In which case proper aliasing would be required to comply with a type constructor with one parameter. How to do it is out-of-scope of this blog, so we will simply continue using `F` and consider that it's a type that can express a successful computation or a failure. We will later come back to it an explain why we keep it as `F`.

`F` can also be an `Option`. In that case, the semantics is slightly different. Instead of returning an error we silently ignore the command. Very useful for idempotent **Commands**. 

## In the begining there is None
It turns out that we don't have an **Aggregate** at the beginning. At some point an initial shot must be given to create the **Aggregate**. So, instead of modeling our functions as above, we should have:
```scala
type Events = Seq[Event]
type State = Option[Aggregate]

State => Command => F[Events]
State => Event => State

// from which we can derive
State => Command => F[(State, Events)]
```
Whenever we have a `None` as value for `State`, we say that we are handling a **Seed Command** or a **Seed Event**. Basically, given a `None` and a **Command** we must decide if the conditions are met to emit the first **Event** that will effectivelly initialise the **Aggregate**. In other words, the **Seed Command** emits the **Seed Event** that moves the state from `None` to `Some[Aggregate]`.

The **Event Handler** requires some extra comment. Note that it is a function from `State => Event => State`. However, in most of the cases we will transition from `None` to a new `Some[Aggregate]` or from `Some[Aggregate]` to an updated `Some[Aggregate]`. A transition from `Some[Aggregate]` to `None` although rare could be possible and would represent the equivalent of a hard delete.

The **Event Handler** is also a function perfectly suited for a `foldLeft` where the accumulator is `State` as we will see next. 

## An Aggregate aggregates its past

The shape of the **Event Handler** function reveals that an **Aggregate** is an accumulation of its past events. We start with a `None` and a **Seed Event**, we generate the first **Aggregate**'s instance and subsequentially we update it by applying the next events to it, one by one.

```scala
  val eventHandler: State => Event => State = ...
  val pastEvents: Events = ...
  
  pastEvents.foldLeft(None:State) { (agg, evt) => eventHandler(agg)(evt) }
```
An **Aggregate**'s past events represents its **History** that when `foldLeft` re-builds the **Aggregate**. Therefore we can say that an **Aggregate**, in the presence of **Event Handlers** is equivalent to its **History**.  Note that this has no relation with the notion of [Equivalence Relation](https://en.wikipedia.org/wiki/Equivalence_relation). The equivalence in this context is supported by the **Event Handlers**, without them it is not possible to re-build the **Aggregate**. Moreover, it is unidirectional. We can't rebuild a **History** from an **Aggregate**. Well, technically we could, but it would be a fabrication not the real history. 

For obvious reasons we can represent the **History** as `Seq[Event]` and we can reformulate our functions to:

```scala
type History = Seq[Event]
History => Command => F[History]
History => Event => History

// from which we can derive, 
type NewEvents = Seq[Event]
History => Command => F[(History, NewEvents)] // augmented history + new Events

// and finally
History => Command => F[History] // return new agumented history
```
There are a few interesting consequences when we express it on those terms.   

Firstly, we can completely remove the `Option[Aggregate]`. As a matter of fact an empty **History** is the equivalent of `None:Option[Aggregate]`.

Secondly, the final funciton shows that we can replace `(Aggregate, Events)` by  **History**.

At that point we can conclude that the most fundamental function on the command side of Event Sourced application is:

```scala 
History => Command => F[History]  
```
And it boils down to:
>  given a previous **History** (may be empty) and a new **Command**, a new augmented **History** is produced or an error is returned.

## ... but that's not so practical

We don't want to work with a function like `History => Command => F[History]` because it's very hard to decide if a **Command** is valid or not without aggregating data. 

Here the world "aggregate" is used with a slightly different meaning. So far we have been using the world **Aggregate** in the context of DDD, but in the context of Event Sourcing an Aggregate can be seen as a model that aggregates data in the same way of an accumulator in a `foldLeft` operation. We need to reduce the **History** to something that we can reason about and make decisions upon. Hence, an aggregate aggregates its past. 

That may sound surprising, but the sole purpose of the **Command Side** is to offer the means to identify what are the possible next events for a given **Aggregate** instance. It's not about querying the state nor about a fancy CRUD model. It's only about agumenting the **History**. That said, the only reason why we should bring an **Aggregate** into memory is to decide  which **Events** may be emitted next, nothing else.
 
In that respect, a funciton like `History => Command => F[History]` is essentially everything that is needed for the **Command Side**, but for practical reasons we will prefer the more explicit ones: `State => Command => F[Events]` (**Command Handler**) and `State => Event => State` (**Event Handler**). 

## Conclusion

We have described the shape of the two functions that are necessary to build the command side of an CQRS application, namely the **Command Handler** and the **Event Handler**. 

We have seen that, based on these two functions we can define a more abstract one that represents the essence of a command model, ie: "given a previous **History** and a new **Command**, a new augmented **History** is produced or an error is returned".

We can also conclude that in an **Event Sourcing** context, an **Aggregate** is an optimization that help us make business decisions based on previous events. At that point the two meanings of an aggregate converge. From a **DDD** perspective, an aggregate defines a consistency boundary enforced by business rules that guarantee the consistency of the model. From an **Event Sourcing** perspective, an aggregate is that accumulator (think `foldLeft`) that reduces the history of past events to one single model.

On part 2 of this serie I will continue in the "but-that-is-not-so-practical" mode and explain why definning **Command Handlers** and the **Event Handlers** can be tedious, especially when the state of our **Aggregate** is modelled as `Option[Aggregate]`. 

