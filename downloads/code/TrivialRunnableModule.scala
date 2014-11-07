  trait TrivialRunnableModule
    extends RunnableModule {
    type D[Z] <: TrivialRunnable[Z]
    type In = Unit
    type Out[Z] = D[Z]
    trait TrivialRunnable[A]
      extends Runnable[A] { da: D[A] =>
      override def run(in: In): Out[A] =
        da
    }
  }
