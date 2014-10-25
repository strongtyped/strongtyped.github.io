    trait StatefulSourceUsage {
      import Lib.TraversablePolynomialModule
      val tpm: TraversablePolynomialModule
  
      import Lib.StatefulSourceModule
  
      object Status extends Enumeration {
        val FREE, BUSY = Value
      }
  
      object Action extends Enumeration {
        val COIN_IN, CANDY_OUT = Value
      }
  
      import Status.{ FREE, BUSY }
      import Action.{ COIN_IN, CANDY_OUT }
  
      case class Machine(status: Status.Value, candies: Int, coins: Int) {
        def acceptsCoinIn = candies != 0 && status == FREE
        def acceptsCandyOut = candies != 0 && status == BUSY
      }
  
      val ssm: StatefulSourceModule {
        type S = Machine
        type In = S
        type Out[Z] = S ** Z
      }
      import ssm._
  
      def executeAction: Action.Value => D[Unit] =
        action => exec {
          machine =>
            action match {
              case COIN_IN if (machine.acceptsCoinIn) =>
                Machine(BUSY, machine.candies, machine.coins + 1)
              case CANDY_OUT if (machine.acceptsCandyOut) =>
                Machine(FREE, machine.candies - 1, machine.coins)
              case _ =>
                machine
            }
        }
  
      val initialMachine: Machine
      val reducer: tpm.Reducer[Unit, Unit]
      val actions: tpm.D[Action.Value]
  
      lazy val ss: ssm.D[Unit] =
        actions.foreach(ssm)(executeAction)(reducer)
  
      def stateful_source_usage() {
        val finalMachine: Machine = ss.run(initialMachine)._1
        println(finalMachine)
      }
    }
