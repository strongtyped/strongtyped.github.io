    def ++[G[+_]](`g~>h`: G ~> H) =
      new (({ type `F++G`[+Z] = F[Z] ++ G[Z] })#`F++G` ~> H) {
        override def apply[Z](`fz++gz`: F[Z] ++ G[Z]): H[Z] =
          `fz++gz` match {
            case Left(fz) => `f~>h`(fz)
            case Right(gz) => `g~>h`(gz)
          }
      }
