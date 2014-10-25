    trait PolynomialUsage {
      import Lib.PolynomialModule
  
      val pm: PolynomialModule
      import pm._
  
      val polynomial01a: Int ** Int => D[Int] => D[Int ** Int] = {
        case (i1, i2) =>
          di =>
            one(i1) ** one(i2) or (one(i1) or one(i2)) ** di or di ** di
            // a*b + (a + b)*x + x*x = (a + x)*(b + x)
      }
  
      val polynomial01b: Int ** Int => D[Int] => D[Int ** Int] = {
        case (i1, i2) =>
          di =>
            di ** di or di ** (one(i1) or one(i2)) or one(i1) ** one(i2)
      }
  
      val polynomial03a: Int ** Int => D[Int] => D[Int ** Int] = {
        case (i1, i2) =>
          di =>
            (one(i1) or di) ** (one(i2) or di)
      }
  
      val polynomial03b: Int ** Int => D[Int] => D[Int ** Int] = {
        case (i1, i2) =>
          di =>
            (di or one(i1)) ** (di or one(i2))
      }
  
      val polynomial02a: Int ** Int => D[Int] => D[Int ** Int ++ ((Int ++ Int) ** Int) ++ (Int ** Int)] = {
        case (i1, i2) =>
          di =>
            (one(i1) ** one(i2)) ++ (one(i1) ++ one(i2)) ** di ++ di ** di
      }
  
      val polynomial02b: Int ** Int => D[Int] => D[Int ** Int ++ (Int ** (Int ++ Int)) ++ (Int ** Int)] = {
        case (i1, i2) =>
          di =>
            di ** di ++ di ** (one(i1) ++ one(i2)) ++ (one(i1) ** one(i2))
      }
  
      val polynomial04a: Int ** Int => D[Int] => D[(Int ++ Int) ** (Int ++ Int)] = {
        case (i1, i2) =>
          di =>
            (one(i1) ++ di) ** (one(i2) ++ di)
      }
  
      val polynomial04b: Int ** Int => D[Int] => D[(Int ++ Int) ** (Int ++ Int)] = {
        case (i1, i2) =>
          di =>
            (di ++ one(i1)) ** (di ++ one(i2))
      }
  
      lazy val example01 = polynomial01a((1, 2))(zero(()))
      lazy val example02 = polynomial01a((1, 2))(one(3))
      lazy val example03 = polynomial01b((1, 2))(zero(()))
      lazy val example04 = polynomial01b((1, 2))(one(3))
//      lazy val example05 = polynomial03a((1, 2))(zero(()))
//      lazy val example06 = polynomial03a((1, 2))(one(3))
//      lazy val example07 = polynomial03b((1, 2))(zero(()))
//      lazy val example08 = polynomial03b((1, 2))(one(3))
//      lazy val example09 = polynomial02a((1, 2))(zero(()))
//      lazy val example10 = polynomial02a((1, 2))(one(3))
//      lazy val example11 = polynomial02b((1, 2))(zero(()))
//      lazy val example12 = polynomial02b((1, 2))(one(3))
//      lazy val example13 = polynomial04a((1, 2))(zero(()))
//      lazy val example14 = polynomial04a((1, 2))(one(3))
//      lazy val example15 = polynomial04b((1, 2))(zero(()))
//      lazy val example16 = polynomial04b((1, 2))(one(3))
  
      val in: In
  
      def polynomial_usage() {
        println(example01.run(in))
        println(example02.run(in))
        println(example03.run(in))
        println(example04.run(in))
//        println(example05.run(in))
//        println(example06.run(in))
//        println(example07.run(in))
//        println(example08.run(in))
//  
//        println(example09.run(in))
//        println(example10.run(in))
//        println(example11.run(in))
//        println(example12.run(in))
//        println(example13.run(in))
//        println(example14.run(in))
//        println(example15.run(in))
//        println(example16.run(in))
      }
  
      val nats: Int => D[Int] = {
        case i =>
          one(i) or nats(i + 1)
      }
  
      val fibs: Int ** Int => D[Int] = {
        case (i1, i2) =>
          one(i1) or fibs((i2, (i1 + i2)))
      }
  
      def recursion_and_take_usage() {
        lazy val firstTenNats: D[Int] = nats(0).take(100)
        lazy val firstNineFibs: D[Int] = fibs((0, 1)).take(25)
  
        println(firstTenNats.run(in))
        println(firstNineFibs.run(in))
      }
  
    }
