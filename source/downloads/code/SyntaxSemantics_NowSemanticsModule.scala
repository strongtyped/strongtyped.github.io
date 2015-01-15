object NowSemanticsModule {

  case class UnitResource[+Z](u: Unit)
  
  object UnitResourceFactory
    extends ResourceFactory {
    type Rsc[+Z] = UnitResource[Z]
    def acquire[Z]() =
      UnitResource(())
    case class UnitResource[+Z](u: Unit)
      extends Resource[Z] {
      def release() = {}
    }
  }

  case class NowSemantics[+Z](value: Z)

  implicit val nwExcCmpNowSemantics =
    new NwExcCmp[NowSemantics] {
      type Result[+Z] = Z
      type RscFac = UnitResourceFactory.type
      val rscFac = UnitResourceFactory
      private[strongtyped] def exec[Z](ez: NowSemantics[Z])(rscz: RscFac#Rsc[Z]): Result[Z] =
        ez.value
      override def bnd[Z, Y](nsz: NowSemantics[Z])(z2nsy: Z => NowSemantics[Y]): NowSemantics[Y] =
        z2nsy(nsz.value)
      override def and[Z, Y](nsz: NowSemantics[Z])(nsy: NowSemantics[Y]): NowSemantics[(Z, Y)] =
        NowSemantics(nsz.value, nsy.value)
      override def now[Z](z: => Z): NowSemantics[Z] =
        NowSemantics(z)
    }

}
