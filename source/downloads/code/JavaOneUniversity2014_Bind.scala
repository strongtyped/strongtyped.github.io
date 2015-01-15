  implicit class Bind[Z](z: Z) {
    def bind[Y](z2y: Z => Y): Y =
      z2y apply z
  }
