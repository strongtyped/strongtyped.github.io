object SubModule {
  sealed trait <=[F[_], G[_]] {
    def inj[Z](sub: F[Z]): G[Z]

    def prj[Z](sup: G[Z]): Option[F[Z]]
  }

  object <= {
    implicit def subReflect[F[_]] =
      new (F <= F) {
        def inj[Z](sub: F[Z]) =
          sub

        def prj[Z](sup: F[Z]) =
          Some(sup)
      }

    import demo.SumModule._

    implicit def subRight[F[_], G[_]] =
      new (G <= ({ type λ[ω] = F[ω] ++ G[ω] })#λ) {
        def inj[Z](sub: G[Z]) = Right(sub)

        def prj[Z](sup: F[Z] ++ G[Z]) = sup match {
          case Right(gz) => Some(gz)
          case Left(_) => None
        }
      }

    implicit def subRecLeft[F[_], G[_], H[_]](implicit f_sub_g: F <= G) =
      new (F <= ({ type λ[ω] = G[ω] ++ H[ω] })#λ) {
        def inj[Z](sub: F[Z]) = Left(f_sub_g.inj(sub))

        def prj[Z](sup: G[Z] ++ H[Z]) = sup match {
          case Left(gz) => f_sub_g.prj(gz)
          case Right(_) => None
        }
      }
  }
}
