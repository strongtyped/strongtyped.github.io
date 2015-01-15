  object ZeroOrOneModule
    extends TraversablePolynomialSourceModule
    with TrivialRunnableModule {
    type D[Z] = ZeroOrOne[Z]
    override def zero[Z]: Unit => D[Z] =
      _ =>
        Zero()
    override def one[Z]: Z => D[Z] =
      z =>
        One(z)
    type Reducer[Z, R] = (Unit => R) ** (Z => R)
    def identityReducer[Z]: Reducer[Z, D[Z]] =
      (zero, one)
    override def lift[Z, Y, R](mm: MM)(z2mmdy: Z => mm.D[Y]): Reducer[Y, R] => Reducer[Z, mm.D[R]] = {
      case (u2r, y2r) => {
        val u2mmdr: Unit => mm.D[R] = {
          case () =>
            mm.one(u2r(()))
        }
        val z2mmdr: Z => mm.D[R] = {
          case z =>
            z2mmdy(z) end {
              y =>
                y2r(y)
            }
        }
        (u2mmdr, z2mmdr)
      }
    }
    sealed trait ZeroOrOne[A]
      extends TraversablePolynomialSource[A]
      with TrivialRunnable[A] { da: D[A] =>
      override def take(n: Int): D[A] =
        da
      override def reducedBy[R](a_r2_r: Reducer[A, R]): R = da match {
        case Zero() =>
          a_r2_r match {
            case (u2r, _) =>
              u2r(())
          }
        case One(a) =>
          a_r2_r match {
            case (_, a2r) =>
              a2r(a)
          }
      }
      override def bnd[B](a2db: A => D[B]): D[B] =
        da reducedBy (zero[B], a2db)
      override def or(da1: => D[A]): D[A] = {
        val u2da: Unit => D[A] = {
          case () =>
            da1
        }
        val a2da: A => D[A] = {
          case a =>
            one(a)
        }
        da reducedBy (u2da, a2da)
      }
      override def toString: String = {
        val u2s: Unit => String = {
          case () =>
            ""
        }
        val a2s: A => String = {
          case a =>
            s" ${a}"
        }
        s"{${da reducedBy (u2s, a2s)} }"
      }
    }
    private case class Zero[A]() extends ZeroOrOne[A]()
    private case class One[A](a: A) extends ZeroOrOne[A]
  }
