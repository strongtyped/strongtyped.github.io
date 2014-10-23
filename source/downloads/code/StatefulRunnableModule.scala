  trait StatefulRunnableModule
    extends RunnableModule {
    type D[Z] <: StatefulRunnable[Z]
    type S
    type In = S
    type Out[Z] = S ** Z
    trait StatefulRunnable[A]
      extends Runnable[A] { da: D[A] =>
    }
  }
