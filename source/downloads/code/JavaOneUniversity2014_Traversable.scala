  trait TraversableModule
    extends ReducibleModule {
    type D[Z] <: Traversable[Z]
    type MM = MultiplicativeModule
    def lift[Z, Y, R](mm: MM)(z2mmy: Z => mm.D[Y]): Reducer[Y, R] => Reducer[Z, mm.D[R]]
    def swap[Z](mm: MM): D[mm.D[Z]] => mm.D[D[Z]] =
      _.foreach(mm)(identity)(identityReducer)
    trait Traversable[A]
      extends Reducible[A] { da: D[A] =>
      def foreach[B, R](mm: MM)(a2mmdb: A => mm.D[B]): Reducer[B, R] => mm.D[R] =
        lift(mm)(a2mmdb) andThen da.reducedBy
    }
  }
