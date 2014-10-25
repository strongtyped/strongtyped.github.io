object FreeModule {

  import demo.ProductModule._
  import demo.ComputationModule._
  import demo.SubModule._
  import demo.TransModule._

  object Free {
    def res[F[_], Z](fz: F[Z]): Free[F, Z] =
      Res(fz)
  }

  sealed trait Free[F[_], Z] { free_z =>
    def and[Y](free_z2y: Free[F, Z => Y]): Free[F, Y] =
      And(free_z, free_z2y)

    def bnd[Y](z2free_y: Z => Free[F, Y]): Free[F, Y] =
      Bnd(free_z, z2free_y)

    def end[Y](z2y: Z => Y)(implicit resultF: ResModule.Res[F]): Free[F, Y] =
      free_z and Free.res(resultF.res(z2y))

    def fold[G[_]: Computation](implicit f_trans_g: F ~> G): G[Z] =
      free_z match {
        case Res(fz) =>
          f_trans_g(fz)
        case And(free_x, free_x2z) =>
          Computation[G].and(free_x.fold)(free_x2z.fold)
        case Bnd(free_x, x2free_z) =>
          Computation[G].bnd(free_x.fold)(x2free_z(_).fold)
      }
  }

  case class Res[F[_], Z](fz: F[Z])
    extends Free[F, Z]

  case class And[F[_], X, Z](free_x: Free[F, X], free_x2z: Free[F, X => Z])
    extends Free[F, Z]

  case class Bnd[F[_], X, Z](free_x: Free[F, X], x2free_z: X => Free[F, Z])
    extends Free[F, Z]

  def lift[F[_], G[_], Z](fz: F[Z])(implicit f_sub_g: F <= G): Free[G, Z] =
    Free.res(f_sub_g.inj(fz))
}
