  object ZeroOrOnePolynomialSourceUsage
    extends PolynomialSourceUsage {
    import Instances.ZeroOrOneModule
    override val psm: ZeroOrOneModule.type = ZeroOrOneModule
    override val in = ()
  }
