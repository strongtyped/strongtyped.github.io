  trait AdditiveModule
    extends HasZeroModule {
    type D[Z] <: Additive[Z]
    trait Additive[A]
      extends HasZero[A] { da: D[A] =>
      def take(n: Int): D[A]
      def or(da1: => D[A]): D[A]
      def ++[B](db: => D[B]): D[A ++ B]
    }
  }
