object ReactiveFutureSemanticsModule {

  implicit def block2callable[Z](z: => Z): Callable[Z] =
    new Callable[Z] {
      override def call(): Z =
        z
    }

  trait Future[+Z] {
    val call: Handler[Z] => Unit
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

  type ReactiveFutureSemantics[+Z] = ExecutorService => Future[Z]

  implicit val rctFtrExcCmpFutureSemantics =
    new RctFtrExcCmp[ReactiveFutureSemantics] {
      type Result[+Z] = Z
      type RscFac = ExecutorServiceResourceFactory.type
      val rscFac = ExecutorServiceResourceFactory
      private[strongtyped] def exec[Z](rfsz: ReactiveFutureSemantics[Z])(rscz: RscFac#Rsc[Z]): Result[Z] = {
        val atomicReference = new AtomicReference[Z]()
        val countDownLatch = new CountDownLatch(1)
        val fz = rfsz(rscz.es)
        fz.call { z =>
          atomicReference.set(z)
          countDownLatch.countDown
        }
        countDownLatch.await
        val rz: Result[Z] = atomicReference.get
        rz
      }
      override def bnd[Z, Y](rfsz: ReactiveFutureSemantics[Z])(z2rfsy: Z => ReactiveFutureSemantics[Y]): ReactiveFutureSemantics[Y] =
        es => new Future[Y] {
          override val call: Handler[Y] => Unit =
            hy => {
              val fz =
                rfsz(es)
              fz call { z =>
                val fy =
                  z2rfsy(z)(es)
                fy call hy
              }
            }
        }
      override def and[Z, Y](rfsz: ReactiveFutureSemantics[Z])(rfsy: ReactiveFutureSemantics[Y]): ReactiveFutureSemantics[Z ** Y] =
        es => new Future[Z ** Y] {
          override val call: Handler[Z ** Y] => Unit =
            h_zny => {
              var oz: Option[Z] =
                None
              var oy: Option[Y] =
                None
              val fz =
                rfsz(es)
              val fy =
                rfsy(es)
              val combiner =
                Actor[Z ++ Y](es) {
                  case Left(z) =>
                    if (oy.isDefined)
                      es.submit {
                        h_zny((z, oy.get))
                      }
                    else
                      oz = Some(z)
                  case Right(y) =>
                    if (oz.isDefined)
                      es.submit {
                        h_zny((oz.get, y))
                      }
                    else
                      oy = Some(y)
                }
              fz call { z =>
                combiner ! Left(z)
              }
              fy call { y =>
                combiner ! Right(y)
              }
            }
        }
      override def now[Z](z: => Z): ReactiveFutureSemantics[Z] =
        es => new Future[Z] {
          override val call: Handler[Z] => Unit =
            hz =>
              hz(z)
        }
      override def activeFuture[Z](z: => Z): ReactiveFutureSemantics[Z] =
        es => new Future[Z] {
          override val call: Handler[Z] => Unit =
            hz =>
              es.submit {
                hz(z)
              }
        }
      override def reactiveFuture[Z](hz_2_u: Handler[Z] => Unit): ReactiveFutureSemantics[Z] =
        es => new Future[Z] {
          override val call: Handler[Z] => Unit = {
            hz_2_u
          }
        }
    }
}
