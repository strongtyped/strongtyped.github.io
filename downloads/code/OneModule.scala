  object OneModule
    extends TraversableSourceModule
    with TrivialRunnableModule {
    type D[Z] = One[Z]
    override def one[Z]: Z => D[Z] =
      z =>
        One(z)
    type Reducer[Z, R] = Z => R
    def identityReducer[Z]: Reducer[Z, D[Z]] =
      one
    override def lift[Z, Y, R](mm: MM)(z2mmdy: Z => mm.D[Y]): Reducer[Y, R] => Reducer[Z, mm.D[R]] = {
      case y2r => {
        val z2mmdr: Z => mm.D[R] = {
          case z =>
            z2mmdy(z) end {
              y =>
                y2r(y)
            }
        }
        z2mmdr
      }
    }
    case class One[A](a: A)
      extends TraversableSource[A]
      with TrivialRunnable[A] { da: D[A] =>
      override def reducedBy[R](a_r2_r: Reducer[A, R]): R = da match {
        case One(a) =>
          a_r2_r match {
            case a2r =>
              a2r(a)
          }
      }
      override def bnd[B](a2db: A => D[B]): D[B] =
        da reducedBy a2db
      override def toString: String = {
        val a2s: A => String = {
          case a =>
            s" ${a}"
        }
        s"{${da reducedBy a2s} }"
      }
    }
  }
