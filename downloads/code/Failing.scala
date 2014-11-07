  trait FailingModule
    extends HasZeroModule {
    val mm: MonoidModule
    import mm._
    type D[Z] <: Failing[Z]
    def fail[Z]: M => D[Z]
    override def zero[Z]: Unit => D[Z] =
      _ => fail(neutral)
    trait Failing[A]
      extends HasZero[A] { da: D[A] =>
    }
  }
