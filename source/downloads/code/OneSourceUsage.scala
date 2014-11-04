  object OneSourceUsage
    extends SourceUsage {
    import Instances.OneModule
    override val sm: OneModule.type = OneModule
    override val in = ()
  }
