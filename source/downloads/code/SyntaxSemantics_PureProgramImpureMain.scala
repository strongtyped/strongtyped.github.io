object PureProgramImpureMain {
  type Id[+Z] = Z

  implicit class CmpOps[Z](z: Id[Z]) {
    def bnd[Y](z2y: Z => Id[Y]): Id[Y] =
      z2y(z)
    def and[Y](y: Id[Y]): Id[(Z, Y)] =
      (z, y)
  }

  val bndProgram: Id[Int] =
    1 bnd { z =>
      1 bnd { y =>
        z + y
      }
    }

  val plus: Int ** Int => Int = {
    case (z, y) => z + y
  }

  val andProgram: Id[Int] =
    (1 and 1) bnd plus

  private def bndMain() {
    val writer = System.console.writer()
    writer.println(bndProgram)
    writer.flush
  }
  private def andMain() {
    val writer = System.console.writer()
    writer.println(andProgram)
    writer.flush
  }

  def main(args: Array[String]): Unit = {
    bndMain()
    andMain()
  }
}
