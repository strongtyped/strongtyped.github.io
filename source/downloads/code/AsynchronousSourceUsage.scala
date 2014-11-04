  trait AsynchronousSourceUsage {

    import Lib.TraversablePolynomialModule
    import Lib.AsynchronousSourceModule

    val tpm: TraversablePolynomialModule
    import tpm.one
    val asm: AsynchronousSourceModule
    import asm.async

    import java.lang.Thread.sleep

    def sleepVerbose(i: Int): Unit => Int = {
      _ =>
        (1 to 10).toList.foreach {
          x =>
            sleep(500)
            if (x == 10) print(s"\n${i} finished\n")
            else print(s"${i} ")
        }
        i
    }

    import math._

    def randomSleep(timeOut: TimeOut) =
      sleep(round(timeOut * random))

    def randomSleepVerbose(i: Int): Unit => Int = {
      _ =>
        (1 to 10).toList.foreach {
          x =>
            randomSleep(500)
            if (x == 10) print(s"\n${i} finished\n")
            else print(s"${i} ")
        }
        i
    }

    val manySleepVerbose: Int => tpm.D[Unit => Int] =
      n =>
        one(sleepVerbose(n)) or manySleepVerbose(n + 1)

    val manyRandomSleepVerbose: Int => tpm.D[Unit => Int] =
      n =>
        one(randomSleepVerbose(n)) or manyRandomSleepVerbose(n + 1)

    val in: asm.In

    val tp_as01: Int => tpm.D[asm.D[Int]] =
      manySleepVerbose(_) end async take (8)

    lazy val as_tp01: Int => asm.D[tpm.D[Int]] =
      tp_as01 andThen tpm.swap(asm)

    val tp_as02: Int => tpm.D[asm.D[Int]] =
      manyRandomSleepVerbose(_) end async take (8)

    lazy val as_tp02: Int => asm.D[tpm.D[Int]] =
      tp_as02 andThen tpm.swap(asm)

    def asynchronous_source_usage() {
      println(as_tp01(1).run(in))
      println(as_tp02(1).run(in))
    }
  }
