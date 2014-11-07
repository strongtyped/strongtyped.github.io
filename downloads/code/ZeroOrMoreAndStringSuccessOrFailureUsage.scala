  object ZeroOrMoreAndStringSuccessOrFailureUsage
    extends TraversablePolynomialAndPolynomialFailingUsage {
    import Instances.ZeroOrMoreModule
    import Instances.StringSuccessOrFailureModule

    override val tpm: ZeroOrMoreModule.type = ZeroOrMoreModule
    override val pfm: StringSuccessOrFailureModule.type = StringSuccessOrFailureModule

    import pfm.mm._

    override val m1 = StringMonoid("failure1")
    override val m2 = StringMonoid("failure2")

    override val tpm_in = ()
    override val mfm_in = ()
  }
