  trait AsynchronousRunnableModule
    extends RunnableModule {
    type D[Z] <: AsynchronousRunnable[Z]
    type In = ExecutorService
    type Out[Z] = Z
    trait AsynchronousRunnable[A]
      extends Runnable[A] { da: D[A] =>
    }
  }
