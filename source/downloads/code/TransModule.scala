object TransModule {
  import demo.SumModule._

  sealed trait ~>[F[_], H[_]] { f_trans_h =>
    def apply[Z](fz: F[Z]): H[Z]

    def ++[G[_]](g_trans_h: G ~> H) =
      new (({ type λ[ω] = F[ω] ++ G[ω] })#λ ~> H) {
        def apply[Z](fz_sum_gz: F[Z] ++ G[Z]): H[Z] = fz_sum_gz match {
          case Left(fz) => f_trans_h(fz)
          case Right(gz) => g_trans_h(gz)
        }
      }
  }
}
