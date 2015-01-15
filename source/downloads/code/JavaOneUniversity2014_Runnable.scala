  trait RunnableModule {
    type D[Z] <: Runnable[Z]
    type In
    type Out[Z]
    trait Runnable[A] { da: D[A] =>
      def run(in: In): Out[A]
    }
  }
