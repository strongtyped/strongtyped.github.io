object ResModule {
  trait Res[F[_]] {
    def res[Z](z: => Z): F[Z]
  }

  object Res {
    def apply[F[_]: Res] = implicitly[Res[F]]
  }
}

object AndModule {
  trait And[F[_]] {
    def and[Z, Y](fz: F[Z])(f_z2y: F[Z => Y]): F[Y]
  }

  object And {
    def apply[F[_]: And] = implicitly[And[F]]
  }
}

object BndModule {
  trait Bnd[F[_]] {
    def bnd[Z, Y](fz: F[Z])(z2fy: Z => F[Y]): F[Y]
  }

  object Bnd {
    def apply[F[_]: Bnd] = implicitly[Bnd[F]]
  }
}

object ComputationModule {
  import AndModule.And
  import BndModule.Bnd

  trait Computation[F[_]] extends And[F] with Bnd[F] {
  }

  object Computation {
    def apply[F[_]: Computation] = implicitly[Computation[F]]
  }
}
