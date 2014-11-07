  object AsyncModule
    extends AsynchronousSourceModule
    with AsynchronousRunnableModule {
    private implicit def toSimpleFuture[Z](to2z: TimeOut => Z) =
      new SimpleFuture[Z] {
        override def get(timeOut: TimeOut): Z =
          to2z(timeOut)
      }
    private implicit def toAsync[Z](es_2_sfz: ExecutorService => SimpleFuture[Z]) =
      Async(es_2_sfz)
    private implicit def toRunnable(u2u: Unit => Unit) =
      new java.lang.Runnable() { def run() = u2u(()) }
    type D[Z] = Async[Z]
    override def one[Z]: Z => D[Z] =
      z => {
        val es_2_sfz: ExecutorService => SimpleFuture[Z] =
          _ => (_: TimeOut) => z
        es_2_sfz
      }
    override def async[Z]: (Unit => Z) => D[Z] =
      u2z => {
        val es_2_sfz: ExecutorService => SimpleFuture[Z] =
          es => {
            val atomic: Atomic[Z] = new Atomic()
            es.submit {
              (_: Unit) =>
                atomic.setValue(u2z(()))
            }
            (timeOut: TimeOut) => {
              atomic.getValue(timeOut)
            }
          }
        es_2_sfz
      }      
    case class Async[A](es_2_sfa: ExecutorService => SimpleFuture[A])
      extends AsynchronousSource[A]
      with AsynchronousRunnable[A] { da: D[A] =>
      override def bnd[B](a2db: A => D[B]): D[B] = {
        val es_2_sfb: ExecutorService => SimpleFuture[B] =
          es => (timeOut: TimeOut) => {
            val startTime = currentTimeMillis()
            val a = es_2_sfa(es).get(timeOut)
            val stopTime = currentTimeMillis()
            (a2db(a)).es_2_sfa(es).get(timeOut - (stopTime - startTime))
          }
        es_2_sfb
      }
      // comment this one out
      // for
      // sequential
      // instead of parallel
      override def and[B](d_a2b: => D[A => B]): D[B] = {
        val es_2_sfb: ExecutorService => SimpleFuture[B] =
          es => {
            val sfa = es_2_sfa(es)
            val sf_a2b = d_a2b.es_2_sfa(es)
            val sfb = (timeOut: TimeOut) => {
              val startTime = currentTimeMillis()
              val a = sfa.get(timeOut)
              val stopTime = currentTimeMillis()
              val a2b = sf_a2b.get(timeOut - (stopTime - startTime))
              a2b(a)
            }
            sfb
          }
        es_2_sfb
      }
      def run(es: ExecutorService): A =
        es_2_sfa(es).get()
    }
  }
