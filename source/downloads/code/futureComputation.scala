  implicit val futureComputation =
    new Computation[Future] {
      override def bnd[Z, Y](fz: Future[Z])(z2fy: Z => Future[Y]): Future[Y] =
        es =>
          (timeOut: TimeOut) => {
            val sfz = fz(es)
            val startTime = currentTimeMillis()
            val z = sfz.get(timeOut)
            val stopTime = currentTimeMillis()
            z2fy(z)(es).get(timeOut - (stopTime - startTime))
          }

      override def and[Z, Y](fz: Future[Z])(f_z2y: Future[Z => Y]): Future[Y] =
        es => {
          val sfz = fz(es)
          val sf_z2y = f_z2y(es)
          (timeOut: TimeOut) => {
            val startTime = currentTimeMillis()
            val z = sfz.get(timeOut)
            val stopTime = currentTimeMillis()
            val z2y = sf_z2y.get(timeOut - (stopTime - startTime))
            z2y(z)
          }
        }
    }
