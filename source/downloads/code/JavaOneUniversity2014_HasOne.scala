  trait HasOneModule
    extends RunnableModule {
    type D[Z] <: HasOne[Z]
    def one[Z]: Z => D[Z]
    trait HasOne[A]
      extends Runnable[A] { da: D[A] =>
    }
  }
