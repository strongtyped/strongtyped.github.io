  object StateUsage
    extends StatefulSourceUsage {
    import Instances.ZeroOrMoreModule

    override val tpm: ZeroOrMoreModule.type = ZeroOrMoreModule

    import Instances.StateModule

    object MachineStateModule
      extends StateModule {
      type S = Machine
    }

    override val ssm: MachineStateModule.type = MachineStateModule
    import ssm._

    import Status.{ FREE, BUSY }

    override val initialMachine = Machine(FREE, 20, 0)

    import Action.{ COIN_IN, CANDY_OUT }

    import tpm.one

    override val actions: tpm.D[Action.Value] =
      one(COIN_IN) or one(CANDY_OUT) or one(COIN_IN) or one(CANDY_OUT)

    override val reducer: tpm.Reducer[Unit, Unit] =
      ({ case _ => () }, { case (_, _) => () })
  }
