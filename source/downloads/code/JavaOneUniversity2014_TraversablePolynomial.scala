  trait TraversablePolynomialModule
    extends PolynomialModule
    with TraversableModule {
    type D[Z] <: TraversablePolynomial[Z]
    trait TraversablePolynomial[A]
      extends Polynomial[A]
      with Traversable[A] { da: D[A] =>
    }
  }
