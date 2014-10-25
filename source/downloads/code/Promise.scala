  case class Promise[Z]() {

    var future: Future[Z] = _

    import java.util.concurrent.CountDownLatch

    class Atomic[Z] {
      var z: Z = _
      val countDownLatch: CountDownLatch = new CountDownLatch(1)

      def getValue(timeOut: TimeOut) = {
        countDownLatch.await(timeOut, MILLISECONDS)
        z
      }

      def setValue(newZ: Z) {
        z = newZ
        countDownLatch.countDown()
      }
    }

    def fulFillWith(block_z: => Z) {
      implicit def toRunnable(u2u: Unit => Unit) =
        new java.lang.Runnable() {
          def run() = u2u(())
        }
      val atomic = new Atomic[Z]()
      future =
        es => {
          es.submit { (_: Unit) =>
            atomic.setValue(block_z)
          }
          (timeOut: TimeOut) =>
            atomic.getValue(timeOut)
        }
    }
  }
