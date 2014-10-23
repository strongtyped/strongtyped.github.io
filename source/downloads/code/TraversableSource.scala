  trait TraversableSourceModule
    extends SourceModule
    with TraversableModule {
    type D[Z] <: TraversableSource[Z]
    trait TraversableSource[A]
      extends Source[A]
      with Traversable[A] { da: D[A] =>
    }
  }
