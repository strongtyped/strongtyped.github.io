  def id[F[+_]]: F ~> F =
    new (F ~> F) {
      override def apply[Z](fz: F[Z]): F[Z] =
        fz
    }    
