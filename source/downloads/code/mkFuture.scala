  def mkFuture[Z](block_z: => Z): Future[Z] = {
    val promise = Promise[Z]()
    promise.fulFillWith(block_z)
    promise.future
  }
