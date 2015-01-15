  trait PolynomialFailingModule
    extends PolynomialModule
    with FailingModule {
    type D[Z] <: PolynomialFailing[Z]
    trait PolynomialFailing[A]
      extends Polynomial[A]
      with Failing[A] { da: D[A] =>
    }
  }
