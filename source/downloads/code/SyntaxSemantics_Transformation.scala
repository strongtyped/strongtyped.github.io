  trait ~>[F[+_], H[+_]] { `f~>h` =>
    def apply[Z](fz: F[Z]): H[Z]
    def andThen[L[+_]](`h~>l`: H ~> L) =
      new (F ~> L) {
        def apply[Z](fz: F[Z]) =
          `h~>l`(`f~>h`(fz))
      }
