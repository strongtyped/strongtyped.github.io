  trait <=[F[+_], G[+_]] {
    val `~>`: F ~> G
  }

  implicit def subReflect[F[+_]] =
    new (F <= F) {
      override val `~>` =
        id[F]
    }

  implicit def subRight[F[+_], G[+_]] =
    new (G <= ({ type `F++G`[+Z] = F[Z] ++ G[Z] })#`F++G`) {
      override val `~>` =
        right[F, G]
    }

  implicit def subTransLeft[F[+_], G[+_], H[+_]](implicit `f<=g`: F <= G) =
    new (F <= ({ type `G++H`[+Z] = G[Z] ++ H[Z] })#`G++H`) {
      override val `~>` =
        `f<=g`.`~>` andThen[({ type `G++H`[+Z] = G[Z] ++ H[Z] })#`G++H`] left
    }
