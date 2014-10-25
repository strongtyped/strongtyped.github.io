  trait SourceModule
    extends MultiplicativeModule {
    type D[Z] <: Source[Z]
    def join[Z]: D[D[Z]] => D[Z] =
      d_dz =>
        d_dz bnd identity
    trait Source[A]
      extends Multiplicative[A] { da: D[A] =>
      def bnd[B](a2db: A => D[B]): D[B]
      override def and[B](d_a2b: => D[A => B]): D[B] =
        da bnd {
          a =>
            d_a2b bnd {
              a2b =>
                one(a2b(a))
            }
        }
      def switch[B](p_a: Predicate[A])(t_a2db: A => D[B])(f_a2db: A => D[B]): D[B] =
        da bnd {
          a =>
            (a bind p_a) match {
              case true =>
                t_a2db(a)
              case false =>
                f_a2db(a)
            }
        }
    }
  }
