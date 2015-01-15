  trait HasZeroModule
    extends RunnableModule {
    type D[Z] <: HasZero[Z]
    def zero[Z]: Unit => D[Z]
    trait HasZero[A]
      extends Runnable[A] { da: D[A] =>
    }
  }
