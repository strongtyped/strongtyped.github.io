  def left[F[+_], G[+_]]: F ~> ({ type `F++G`[+Z] = F[Z] ++ G[Z] })#`F++G` =
    new (F ~> ({ type `F++G`[+Z] = F[Z] ++ G[Z] })#`F++G`) {
      override def apply[Z](fz: F[Z]): F[Z] ++ G[Z] =
        Left(fz)
    }
  def right[F[+_], G[+_]]: G ~> ({ type `F++G`[+Z] = F[Z] ++ G[Z] })#`F++G` =
    new (G ~> ({ type `F++G`[+Z] = F[Z] ++ G[Z] })#`F++G`) {
      override def apply[Z](gz: G[Z]): F[Z] ++ G[Z] =
        Right(gz)
    }
