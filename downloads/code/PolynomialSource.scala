  trait PolynomialSourceModule
    extends SourceModule
    with PolynomialModule {
    type D[Z] <: PolynomialSource[Z]
    trait PolynomialSource[A]
      extends Source[A]
      with Polynomial[A] { da: D[A] =>
      def filter(pa: Predicate[A]): D[A] =
        da.switch(pa) {
          a =>
            a bind one
        } {
          a =>
            () bind zero
        }
    }
  }
