  object ZeroOrMoreAndZeroOrOneUsage
    extends TraversablePolynomialAndPolynomialUsage {
    import Instances.ZeroOrMoreModule
    import Instances.ZeroOrOneModule

    override val tpm: ZeroOrMoreModule.type = ZeroOrMoreModule
    override val pm: ZeroOrOneModule.type = ZeroOrOneModule

    override val tpm_in = ()
    override val pm_in = ()
  }
