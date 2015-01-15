  trait AsynchronousSourceModule
    extends SourceModule
    with AsynchronousModule {
    type D[Z] <: AsynchronousSource[Z]
    trait AsynchronousSource[A]
      extends Source[A]
      with Asynchronous[A] { da: D[A] =>
    }
  }
