//    trait SourceUsage {
//      import Lib.SourceModule
//  
//      val sm: SourceModule
//      import sm._
//  
//      val source01: Int ** Int => D[Int ** Int] = {
//        case (i1, i2) =>
//          one(i1) bnd {
//            z =>
//              one(i2) end {
//                y =>
//                  (z, y)
//              }
//          }
//      }
//  
//      val source02: Int ** Int ** Int => D[Int ** Int ** Int] = {
//        case ((i1, i2), i3) =>
//          one(i1) bnd {
//            z =>
//              one(i2) bnd {
//                y =>
//                  one(i3) end {
//                    x =>
//                      ((z, y), x)
//                  }
//              }
//          }
//      }
//  
//      val source03: Int ** Int ** Int ** Int => D[Int ** Int ** Int ** Int] = {
//        case (((i1, i2), i3), i4) =>
//          one(i1) bnd {
//            z =>
//              one(i2) bnd {
//                y =>
//                  one(i3) bnd {
//                    x =>
//                      one(i4) end {
//                        w =>
//                          (((z, y), x), w)
//                      }
//                  }
//              }
//          }
//      }
//  
//      val source04: Int => D[Int] = {
//        case i =>
//          one(i).switch(_ < 10) {
//            _ =>
//              one(i)
//          } {
//            _ =>
//              one(i * i)
//          }
//      }
//  
//      lazy val example01 = source01((1, 2))
//      lazy val example02 = source02(((1, 2), 3))
//      lazy val example03 = source03((((1, 2), 3), 4))
//      lazy val example04 = source04(5)
//      lazy val example05 = source04(11)
//  
//      val in: In
//  
//      def source_usage() {
//        println(example01.run(in))
//        println(example02.run(in))
//        println(example03.run(in))
//        println(example04.run(in))
//        println(example05.run(in))
//      }
//  
//    }
