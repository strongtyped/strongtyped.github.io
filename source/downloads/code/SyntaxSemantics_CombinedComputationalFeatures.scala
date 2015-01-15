  trait Cmp[C[+_]]
    extends Bnd[C]
    with And[C] {
  }

  object Cmp {
    def apply[C[+_]: Cmp] = implicitly[Cmp[C]]
  }

  trait ExcCmp[EC[+_]]
    extends Exc[EC]
    with Cmp[EC] {
  }

  object ExcCmp {
    def apply[EC[+_]: ExcCmp] = implicitly[ExcCmp[EC]]
  }

  trait Prg[P[+_]]
    extends Res[P]
    with Cmp[P] {
    def end[Z, Y](pz: P[Z])(z2y: Z => Y): P[Y] =
      bnd(pz) { z =>
        res(z2y(z))
      }
  }

  object Prg {
    def apply[P[+_]: Prg] = implicitly[Prg[P]]
  }
