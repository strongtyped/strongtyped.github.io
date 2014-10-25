object EffectModule {

  import demo.FutureModule._
  import demo.IdentityModule._
  import demo.ResModule._
  import demo.SubModule.<=._
  import demo.SubModule._
  import demo.SumModule._
  import demo.SupplierModule._
  import demo.TransModule._
  import demo.WithFileModule._

  implicit val id_sub_effect: Identity <= ({ type λ[ω] = Identity[ω] ++ Supplier[ω] ++ WithFile[ω] })#λ =
    subRecLeft[Identity, ({ type λ[ω] = Identity[ω] ++ Supplier[ω] })#λ, WithFile]

  implicit val supplier_sub_effect: Supplier <= ({ type λ[ω] = Identity[ω] ++ Supplier[ω] ++ WithFile[ω] })#λ =
    subRecLeft[Supplier, ({ type λ[ω] = Identity[ω] ++ Supplier[ω] })#λ, WithFile]

  implicit val withFile_sub_effect: WithFile <= ({ type λ[ω] = Identity[ω] ++ Supplier[ω] ++ WithFile[ω] })#λ =
    subRight[({ type λ[ω] = Identity[ω] ++ Supplier[ω] })#λ, WithFile]

  implicit val effect_trans_future: ({ type λ[ω] = Identity[ω] ++ Supplier[ω] ++ WithFile[ω] })#λ ~> Future =
    identity_trans_future ++ supplier_trans_future ++ withFile_trans_future

  implicit val identityEffectRes =
    new Res[({ type λ[ω] = Identity[ω] ++ Supplier[ω] ++ WithFile[ω] })#λ] {
      override def res[Z](z: => Z): Identity[Z] ++ Supplier[Z] ++ WithFile[Z] =
        Left(Left(Res[Identity].res(z)))
    }

}
