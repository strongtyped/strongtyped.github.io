  object ZeroOrMorePolynomialSourceUsage
    extends PolynomialSourceUsage {
    import Instances.ZeroOrMoreModule
    override val psm: ZeroOrMoreModule.type = ZeroOrMoreModule
    override val in = ()
  }
