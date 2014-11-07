  trait SimpleFuture[Z]
    extends Future[Z] {
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
