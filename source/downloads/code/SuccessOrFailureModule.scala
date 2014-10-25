  trait SuccessOrFailureModule
    extends TraversablePolynomialSourceModule
    with PolynomialFailingModule
    with TrivialRunnableModule {
    import mm._
    type D[Z] = SuccessOrFailure[Z]
    override def fail[Z]: M => D[Z] =
      m => Failure(m)
    override def one[Z]: Z => D[Z] =
      z => Success(z)
    type Reducer[Z, R] = (M => R) ** (Z => R)
    override def identityReducer[Z]: Reducer[Z, D[Z]] = {
      (fail, one)
    }
    override def lift[Z, Y, R](mm: MM)(z2mmdy: Z => mm.D[Y]): Reducer[Y, R] => Reducer[Z, mm.D[R]] = {
      case (m2r, y2r) =>
        val m2mmdr: M => mm.D[R] = {
          case m =>
            mm.one(m2r(m))
        }
        val z2mmdr: Z => mm.D[R] = {
          case z =>
            z2mmdy(z) end {
              y =>
                y2r(y)
            }
        }
        (m2mmdr, z2mmdr)
    }
    sealed trait SuccessOrFailure[A]
      extends TraversablePolynomialSource[A]
      with PolynomialFailing[A]
      with TrivialRunnable[A] { da: D[A] =>
      override def take(n: Int): D[A] =
        da
      override def reducedBy[R](a_r2_r: Reducer[A, R]): R = da match {
        case Failure(m) =>
          a_r2_r match {
            case (m2r, _) =>
              m2r(m)
          }
        case Success(a) =>
          a_r2_r match {
            case (_, a2r) =>
              a2r(a)
          }
      }

      override def bnd[B](a2ob: A => D[B]): D[B] = {
        val m2ob: M => D[B] = {
          case m =>
            fail(m)
        }
        val reducer: Reducer[A, D[B]] =
          (m2ob, a2ob)
        da reducedBy reducer
      }
      // comment this one out
      // for
      // fail fast
      // instead of
      // fail slow and accumulate
      override def and[B](d_a2b: => D[A => B]): D[B] = {
        val m2ob: M => D[B] = {
          case m =>
            d_a2b match {
              case Success(_) =>
                fail(m)
              case Failure(m1) =>
                fail(m1 operator m)
            }
        }
        val a2ob: A => D[B] = {
          case a =>
            d_a2b match {
              case Success(a2b) =>
                one(a2b(a))
              case Failure(m) =>
                fail(m)
            }
        }
        val reducer: Reducer[A, D[B]] =
          (m2ob, a2ob)
        da reducedBy reducer
      }
      override def or(da1: => D[A]): D[A] = {
        val u2oa: M => D[A] = {
          case m =>
            da match {
              case Success(_) =>
                da
              case Failure(_) =>
                da1
            }
        }
        val a2oa: A => D[A] = {
          case a =>
            one(a)
        }
        val reducer: Reducer[A, D[A]] =
          (u2oa, a2oa)
        da reducedBy reducer
      }
      override def toString: String = {
        val u2s: M => String = {
          case m =>
            s" ${m.toString}"
        }
        val a2s: A => String = {
          case a =>
            s" ${a}"
        }
        val reducer: Reducer[A, String] =
          (u2s, a2s)
        s"{${da reducedBy reducer} }"
      }
      override def run(in: In): Out[A] =
        da
    }
    case class Failure[A](m: M) extends SuccessOrFailure[A]
    case class Success[A](a: A) extends SuccessOrFailure[A]
  }
