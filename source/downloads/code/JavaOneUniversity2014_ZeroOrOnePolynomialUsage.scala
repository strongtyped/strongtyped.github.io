  object ZeroOrOnePolynomialUsage
    extends PolynomialUsage {
    import Instances.ZeroOrOneModule
    override val pm: ZeroOrOneModule.type = ZeroOrOneModule
    override val in = ()
  }
