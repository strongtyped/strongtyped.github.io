  trait MonoidModule {
    type M <: Monoid
    def neutral: M
    trait Monoid { m: M =>
      def operator(m1: M): M
    }
  }
