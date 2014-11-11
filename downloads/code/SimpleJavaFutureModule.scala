object SimpleJavaFutureModule {
  type TimeOut = Long

  import java.lang.Long.MAX_VALUE
  import java.util.concurrent.TimeUnit
  import java.util.concurrent.TimeUnit.MILLISECONDS

  type JavaFuture[Z] = java.util.concurrent.Future[Z]

  trait SimpleJavaFuture[Z]
    extends JavaFuture[Z] {
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

  implicit def toSimpleJavaFuture[Z](timeOut2z: TimeOut => Z) =
    new SimpleJavaFuture[Z] {
      override def get(timeOut: TimeOut): Z =
        timeOut2z(timeOut)
    }
}
