  trait PolynomialModule
    extends MultiplicativeModule
    with AdditiveModule {
    type D[Z] <: Polynomial[Z]
    trait Polynomial[A]
      extends Multiplicative[A]
      with Additive[A] { da: D[A] =>
      override def ++[B](db: => D[B]): D[A ++ B] = {
        val dl: D[A ++ B] =
          da end {
            a =>
              Left(a)
          }
        val dr: D[A ++ B] =
          db end {
            b =>
              Right(b)
          }
        dl or dr
      }
    }
  }
