  trait AsynchronousModule {
    type D[Z] <: Asynchronous[Z]
    def async[Z]: (Unit => Z) => D[Z]
    trait Asynchronous[A] {
    }
  }
