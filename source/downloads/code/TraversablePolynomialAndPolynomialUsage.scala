    trait TraversablePolynomialAndPolynomialUsage {
      import Lib.TraversablePolynomialModule
      import Lib.PolynomialModule
  
      val tpm: TraversablePolynomialModule
      val pm: PolynomialModule
  
      val tp_p: pm.D[Int] ** pm.D[Int] ** pm.D[Int] => tpm.D[pm.D[Int]] = {
        case ((pmd01, pmd02), pmd03) =>
          tpm.one(pmd01) or tpm.one(pmd02) or tpm.one(pmd03)
      }
  
      lazy val p_tp: pm.D[Int] ** pm.D[Int] ** pm.D[Int] => pm.D[tpm.D[Int]] =
        tp_p andThen tpm.swap(pm)
  
      lazy val example01: pm.D[Int] ** pm.D[Int] ** pm.D[Int] =
        ((pm.one(1), pm.one(2)), pm.one(3))
      lazy val example02: pm.D[Int] ** pm.D[Int] ** pm.D[Int] =
        ((pm.zero(()), pm.one(2)), pm.one(3))
      lazy val example03: pm.D[Int] ** pm.D[Int] ** pm.D[Int] =
        ((pm.one(1), pm.zero(())), pm.zero(()))
  
      val tpm_in: tpm.In
      val pm_in: pm.In
  
      def traversablePolynomial_and_polynomial_usage() {
        println(tp_p(example01).run(tpm_in))
        println(p_tp(example01).run(pm_in))
        println(tp_p(example02).run(tpm_in))
        println(p_tp(example02).run(pm_in))
        println(tp_p(example03).run(tpm_in))
        println(p_tp(example03).run(pm_in))
      }
    }
