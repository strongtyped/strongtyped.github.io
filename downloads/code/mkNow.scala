  def mkNow[Z](value_z: Z): Future[Z] =
    _ =>
      (timeOut: TimeOut) =>
        value_z
