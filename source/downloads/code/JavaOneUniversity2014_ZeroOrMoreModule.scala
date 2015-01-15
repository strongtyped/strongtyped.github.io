  object ZeroOrMoreModule
    extends TraversablePolynomialSourceModule
    with TrivialRunnableModule {
    type D[Z] = ZeroOrMore[Z]
    override def zero[Z]: Unit => D[Z] =
      _ =>
        Zero()
    override def one[Z]: Z => D[Z] =
      z =>
        More((z, zero))
    private def more[Z]: (Z ** (Unit => D[Z])) => D[Z] = {
      case z_n_u2dz =>
        More[Z](z_n_u2dz)
    }
    type Reducer[Z, R] = (Unit => R) ** ((Z ** (Unit => R)) => R)
    def identityReducer[Z]: Reducer[Z, D[Z]] = {
      (zero, more)
    }
    override def lift[Z, Y, R](mm: MM)(z2mmdy: Z => mm.D[Y]): Reducer[Y, R] => Reducer[Z, mm.D[R]] = {
      case (u2r, y_n_u2r__2__r) => {
        val u2mmdr: Unit => mm.D[R] = {
          case () =>
            mm.one(u2r(()))
        }
        val z_n_u2mmdr__2__mmdr: (Z ** (Unit => mm.D[R])) => mm.D[R] = {
          case (z, u2mmdr) =>
            z2mmdy(z) and (
              u2mmdr(()) end {
                r =>
                  y =>
                    y_n_u2r__2__r((y, { case () => r }))
              })

        }
        (u2mmdr, z_n_u2mmdr__2__mmdr)
      }
    }
    sealed trait ZeroOrMore[A]
      extends TraversablePolynomialSource[A]
      with TrivialRunnable[A] { da: D[A] =>
      override def take(n: Int): D[A] =
        da match {
          case Zero() =>
            zero(())
          case More((a, u2da)) =>
            if (n > 0)
              more((a, { case () => u2da(()).take(n - 1) }))
            else
              zero(())
        }
      override def bnd[B](a2db: A => D[B]): D[B] = {
        val a_n_u2db__2__db: (A ** (Unit => D[B])) => D[B] = {
          case (a, u2db) =>
            a2db(a) or u2db(())
        }
        da reducedBy ((zero[B], a_n_u2db__2__db))
      }
      override def or(da1: => D[A]): D[A] = {
        val u2da: Unit => D[A] = {
          case () =>
            da1
        }
        da reducedBy (u2da, more[A])
      }
      override def toString: String = {
        val u2s: Unit => String = {
          case () =>
            ""
        }
        val a_n_u2s__2__s: (A ** (Unit => String)) => String = {
          case (a, s) =>
            s" ${a}${s(())}"
        }
        s"[${da reducedBy (u2s, a_n_u2s__2__s)} ]"
      }
      override def reducedBy[R](a_r2_r: Reducer[A, R]): R =
        da match {
          case Zero() =>
            a_r2_r match {
              case (u2r, _) =>
                u2r(())
            }
          case More((a, u2da)) =>
            a_r2_r match {
              case (u2r, a_n_u2r__2__r) =>
                a_n_u2r__2__r((a, { case () => u2da(()).reducedBy((u2r, a_n_u2r__2__r)) }))
            }
        }
      override def run(in: In): Out[A] =
        da
    }

    private case class Zero[A]() extends ZeroOrMore[A]
    private case class More[A](a_n_u2da: A ** (Unit => D[A])) extends ZeroOrMore[A]
  }
