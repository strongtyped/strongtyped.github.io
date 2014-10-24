  object OneMultiplicativeUsage
    extends MultiplicativeUsage {
    import Instances.OneModule
    override val mm: OneModule.type = OneModule
    override val in = ()
  }
