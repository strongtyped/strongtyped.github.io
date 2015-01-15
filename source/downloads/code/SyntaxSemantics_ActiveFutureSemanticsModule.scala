object ActiveFutureSemanticsModule {

  type TimeOut = Long

  trait JavaFuture[Z]
    extends java.util.concurrent.Future[Z] {
    def get(timeOut: TimeOut): Z

    override def get: Z =
      get(MAX_VALUE)

    override def get(timeOut: TimeOut, timeUnit: TimeUnit): Z =
      get(MILLISECONDS.convert(timeOut, timeUnit))

    override def isDone =
      sys.error("not implemented")

    override def isCancelled =
      sys.error("not implemented")

    override def cancel(interruptable: Boolean) =
      sys.error("not implemented")
  }

  implicit def toJavaFuture[Z](timeOut2z: TimeOut => Z) =
    new JavaFuture[Z] {
      override def get(timeOut: TimeOut): Z =
        timeOut2z(timeOut)
    }

  trait ScalaFuture[+Z] {
    def sjf[ZZ >: Z](): JavaFuture[ZZ]
    def get(timeOut: TimeOut): Z =
      sjf().get(timeOut)
    def get(): Z =
      sjf().get()
  }

  implicit def toRunnable(u2u: Unit => Unit) =
    new Runnable() {
      def run() = u2u(())
    }

  implicit def toScalaFuture[Z](timeOut2z: TimeOut => Z) =
    new ScalaFuture[Z] {
      override def sjf[ZZ >: Z](): JavaFuture[ZZ] =
        timeOut2z
    }

  case class ExecutorServiceResource[+Z](es: ExecutorService)

  object ExecutorServiceResourceFactory
    extends ResourceFactory {
    type Rsc[+Z] = ExecutorServiceResource[Z]
    def acquire[Z]() =
      ExecutorServiceResource(Executors.newScheduledThreadPool(8))
    case class ExecutorServiceResource[+Z](es: ExecutorService)
      extends Resource[Z] {
      def release() =
        es.shutdown()
    }
  }

  case class ActiveFutureSemantics[+Z](es2sjf: ExecutorService => ScalaFuture[Z])

  case class Promise[Z]() {
    var activeFutureSemantics: ActiveFutureSemantics[Z] = _
    class Atomic[Z] {
      private[this] var v: Z = _
      val countDownLatch: CountDownLatch = new CountDownLatch(1)
      def getValue(timeOut: TimeOut) = {
        countDownLatch.await(timeOut, MILLISECONDS)
        v
      }
      def setValue(value: Z) {
        v = value
        countDownLatch.countDown()
      }
    }
    def activeFuture(z: => Z): ActiveFutureSemantics[Z] = {
      val atomic = new Atomic[Z]()
      activeFutureSemantics =
        ActiveFutureSemantics { (es: ExecutorService) =>
          es.submit { (_: Unit) =>
            atomic.setValue(z)
          }
          (timeOut: TimeOut) =>
            atomic.getValue(timeOut)
        }
      activeFutureSemantics
    }
  }

  implicit val actFtrExcCmpFutureSemantics =
    new ActFtrExcCmp[ActiveFutureSemantics] {
      type Result[+Z] = Z
      type RscFac = ExecutorServiceResourceFactory.type
      val rscFac = ExecutorServiceResourceFactory
      private[strongtyped] def exec[Z](fz: ActiveFutureSemantics[Z])(rscz: RscFac#Rsc[Z]): Result[Z] =
        fz.es2sjf(rscz.es).get
      override def bnd[Z, Y](fz: ActiveFutureSemantics[Z])(z2fy: Z => ActiveFutureSemantics[Y]): ActiveFutureSemantics[Y] =
        ActiveFutureSemantics { es =>
          (timeOut: TimeOut) =>
            val sfz = fz.es2sjf(es)
            val startTime = currentTimeMillis()
            val z = sfz.get(timeOut)
            val stopTime = currentTimeMillis()
            z2fy(z).es2sjf(es).get(timeOut - (stopTime - startTime))
        }

      override def and[Z, Y](fz: ActiveFutureSemantics[Z])(fy: ActiveFutureSemantics[Y]): ActiveFutureSemantics[(Z, Y)] =
        ActiveFutureSemantics { es =>
          val sfz = fz.es2sjf(es)
          val sfy = fy.es2sjf(es)
          (timeOut: TimeOut) => {
            val startTime = currentTimeMillis()
            val z = sfz.get(timeOut)
            val stopTime = currentTimeMillis()
            val y = sfy.get(timeOut - (stopTime - startTime))
            (z, y)
          }
        }

      override def now[Z](z: => Z): ActiveFutureSemantics[Z] =
        ActiveFutureSemantics { es =>
          (timeOut: TimeOut) =>
            z
        }

      override def activeFuture[Z](z: => Z): ActiveFutureSemantics[Z] =
        Promise[Z]().activeFuture(z)
    }
}
