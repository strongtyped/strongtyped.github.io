  trait ReducibleModule {
    type D[Z] <: Reducible[Z]
    type Reducer[Z, R]
    def identityReducer[Z]: Reducer[Z, D[Z]]
    trait Reducible[A] { da: D[A] =>
      def reducedBy[R](a_r2_r: Reducer[A, R]): R
    }
  }
