  object StringMonoidModule
    extends MonoidModule {
    type M = StringMonoid
    override def neutral =
      StringMonoid("")
    case class StringMonoid(s: String)
      extends Monoid { m: M =>
      override def operator(m1: M) =
        StringMonoid(s"${m.s} -- ${m1.s}")
      override def toString =
        s
    }
  }
