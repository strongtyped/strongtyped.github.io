    object ZeroOrMorePolynomialUsage
      extends PolynomialUsage {
      import Instances.ZeroOrMoreModule
      override val pm: ZeroOrMoreModule.type = ZeroOrMoreModule
      override val in = ()
    }
