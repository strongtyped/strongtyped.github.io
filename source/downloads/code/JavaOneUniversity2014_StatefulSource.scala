  trait StatefulSourceModule
    extends SourceModule
    with StatefulModule {
    type D[Z] <: StatefulSource[Z]
    override def exec: (S => S) => D[Unit] =
      s2s =>
        get(()) bnd {
          s =>
            set(s2s(s))
        }
    trait StatefulSource[A]
      extends Source[A]
      with Stateful[A] { da: D[A] =>
    }
  }
