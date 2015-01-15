  trait TraversablePolynomialAndPolynomialFailingUsage {
    import Lib.TraversablePolynomialModule
    import Lib.PolynomialFailingModule

    val tpm: TraversablePolynomialModule
    val pfm: PolynomialFailingModule

    val tp_pf: pfm.D[Int] ** pfm.D[Int] ** pfm.D[Int] => tpm.D[pfm.D[Int]] = {
      case ((mfmd01, mfmd02), mfmd03) =>
        tpm.one(mfmd01) or tpm.one(mfmd02) or tpm.one(mfmd03)
    }

    lazy val pf_tp: pfm.D[Int] ** pfm.D[Int] ** pfm.D[Int] => pfm.D[tpm.D[Int]] =
      tp_pf andThen tpm.swap(pfm)

    val m1: pfm.mm.M
    val m2: pfm.mm.M

    lazy val example01: pfm.D[Int] ** pfm.D[Int] ** pfm.D[Int] =
      ((pfm.one(1), pfm.one(2)), pfm.one(3))
    lazy val example02: pfm.D[Int] ** pfm.D[Int] ** pfm.D[Int] =
      ((pfm.fail(m1), pfm.one(2)), pfm.one(3))
    lazy val example03: pfm.D[Int] ** pfm.D[Int] ** pfm.D[Int] =
      ((pfm.one(1), pfm.fail(m1)), pfm.fail(m2))

    val tpm_in: tpm.In
    val mfm_in: pfm.In

    def traversablePolynomial_and_multiplicativeFailing_usage() {
      println(tp_pf(example01).run(tpm_in))
      println(pf_tp(example01).run(mfm_in))
      println(tp_pf(example02).run(tpm_in))
      println(pf_tp(example02).run(mfm_in))
      println(tp_pf(example03).run(tpm_in))
      println(pf_tp(example03).run(mfm_in))
    }
  }
