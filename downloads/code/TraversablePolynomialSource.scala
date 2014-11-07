  trait TraversablePolynomialSourceModule
    extends TraversablePolynomialModule
    with PolynomialSourceModule {
    type D[Z] <: TraversablePolynomialSource[Z]
    trait TraversablePolynomialSource[A]
      extends TraversablePolynomial[A]
      with PolynomialSource[A] { da: D[A] =>
    }
  }
