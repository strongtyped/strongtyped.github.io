  trait MultiplicativeModule
    extends HasOneModule {
    type D[Z] <: Multiplicative[Z]
    trait Multiplicative[A]
      extends HasOne[A] { da: D[A] =>
      def and[B](d_a2b: => D[A => B]): D[B]
      def end[B](a2b: A => B): D[B] =
        da and one(a2b)
      def **[B](db: => D[B]): D[A ** B] =
        da and (
          db end {
            b =>
              a =>
                (a, b)
          })
    }
  }
