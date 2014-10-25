  trait MultiplicativeUsage {
    import Lib.MultiplicativeModule

    val mm: MultiplicativeModule
    import mm._

    val multiplicative01: Int => D[Int] => D[Int ** Int] = {
      case i =>
        di =>
          one(i) ** di // 1 * x
    }

    val multiplicative02: Int ** Int => D[Int] => D[Int ** Int ** Int] = {
      case (i1, i2) =>
        di =>
          one(i1) ** one(i2) ** di // 1 * 1 * x
    }

    val multiplicative03: Int ** Int ** Int => D[Int] => D[Int ** Int ** Int ** Int] = {
      case ((i1, i2), i3) =>
        di =>
          one(i1) ** one(i2) ** one(i3) ** di
    }

    val multiplicative04: Int ** Int => D[Int] => D[Int ** (Int ** Int)] = {
      case (i1, i2) =>
        di =>
          one(i1) ** (one(i2) ** di)
    }

    val multiplicative05: Int ** Int => D[Int] = {
      case (i1, i2) =>
        one(i1) and {
          one(i2) end {
            i2 =>
              i1 =>
                i1 + i2
          }
        }
    }

    val multiplicative06: Int ** Int ** Int => D[Int] = {
      case ((i1, i2), i3) =>
        one(i1) and {
          one(i2) and {
            one(i3) end {
              i3 =>
                i2 =>
                  i1 =>
                    i1 + i2 + i3
            }
          }
        }
    }

    val multiplicative07: ((((String, String), String), String)) => D[String] = {
      case (((i1, i2), i3), i4) =>
        one(i1) and {
          one(i2) and {
            one(i3) and {
              one(i4) end {
                i4 =>
                  i3 =>
                    i2 =>
                      i1 =>
                        i1 + i2 + i3 + i4
              }
            }
          }
        }
    }

    lazy val example01 = multiplicative01(1)(one(2)) // one(1) *** one(2)
    lazy val example02 = multiplicative02((1, 2))(one(3))
    lazy val example03 = multiplicative03(((1, 2), 3))(one(4))
    lazy val example04 = multiplicative04((1, 2))(one(3))
    lazy val example05 = multiplicative05((1, 2))
    lazy val example06 = multiplicative06(((1, 2), 3))
    lazy val example07 = multiplicative07(((("a", "b"), "c"), "d"))

    val in: In

    def multiplicative_usage() {
      println(example01.run(in))
      println(example02.run(in))
      println(example03.run(in))
      println(example04.run(in))
      println(example05.run(in))
      println(example06.run(in))
      println(example07.run(in))
    }
  }
