  class Prog[P[+_]: Prg, +Z] { pz: P[Z] =>
    def bnd[Y](z2py: Z => P[Y]): P[Y] =
      Prg[P].bnd(pz)(z2py)
    def seq[Y](py: P[Y]): P[Y] =
      Prg[P].seq(pz)(py)
    def and[Y](py: P[Y]): P[Z ** Y] =
      Prg[P].and(pz)(py)
    def end[Y](z2y: Z => Y): P[Y] =
      Prg[P].end(pz)(z2y)
    def end: P[Unit] =
      pz end { _ =>
        ()
      }
  }
