  trait PolynomialSourceUsage {
    import Lib.PolynomialSourceModule

    val psm: PolynomialSourceModule
    import psm._

    val ps01: Int ** Int => D[Int] => D[Int ** Int] = {
      case (i1, i2) =>
        di =>
          (di bnd {
            i =>
              di end {
                j =>
                  (i, j)
              }
          }) or
            (di bnd {
              i =>
                (one(i1) or one(i2)) end {
                  j =>
                    (i, j)
                }
            }) or
            (one(i1) bnd {
              i =>
                one(i2) end {
                  j =>
                    (i, j)
                }
            })
    }

    val ps02: Int ** Int => D[Int] => D[Int ** Int ++ (Int ** (Int ++ Int)) ++ (Int ** Int)] = {
      case (i1, i2) =>
        di =>
          (di bnd {
            i =>
              di end {
                j =>
                  (i, j)
              }
          }) ++
            (di bnd {
              i =>
                (one(i1) ++ one(i2)) end {
                  j =>
                    (i, j)
                }
            }) ++
            (one(i1) bnd {
              i =>
                one(i2) end {
                  j =>
                    (i, j)
                }
            })
    }

    val fps01: Int ** Int => D[Int] => D[Int ** Int] = {
      case (i1, i2) =>
        di =>
          ps01((i1, i2))(di).filter {
            case (i1, i2) =>
              i1 == i2
          }
    }

    val fps02: Int ** Int => D[Int] => D[Int ** Int ++ (Int ** (Int ++ Int)) ++ (Int ** Int)] = {
      case (i1, i2) =>
        di =>
          ps02((i1, i2))(di).filter {
            case Right(_) =>
              true
            case _ =>
              false
          }
    }

    lazy val example01 = ps01((1, 2))(zero(()))
    lazy val example02 = ps01((1, 2))(one(3))
    lazy val example03 = ps02((1, 2))(zero(()))
    lazy val example04 = ps02((1, 2))(one(3))
    lazy val example05 = fps01((1, 2))(zero(()))
    lazy val example06 = fps01((1, 2))(one(3))
    lazy val example07 = fps02((1, 2))(zero(()))
    lazy val example08 = fps02((1, 2))(one(3))

    val in: In

    def polynomial_source_usage() {
      println(example01.run(in))
      println(example02.run(in))
      println(example03.run(in))
      println(example04.run(in))
      println(example05.run(in))
      println(example06.run(in))
      println(example07.run(in))
      println(example08.run(in))
    }

  }
