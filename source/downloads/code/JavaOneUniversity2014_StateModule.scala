  trait StateModule
    extends StatefulSourceModule
    with StatefulRunnableModule {
    type D[Z] = State[Z]
    private implicit def toState[Z](s_2_snz: S => S ** Z): D[Z] =
      State(s_2_snz)
    override def one[Z]: Z => D[Z] =
      z => (s: S) => (s, z)
    override def get: Unit => D[S] =
      _ => (s: S) => (s, s)
    override def set: S => D[Unit] =
      s => (_: S) => (s, ())
    case class State[A](s_2_sna: S => S ** A)
      extends StatefulSource[A]
      with StatefulRunnable[A] { da: D[A] =>
      override def bnd[B](a2db: A => D[B]): D[B] =
        (s: S) =>
          da.s_2_sna(s) match {
            case (s1, a) =>
              a2db(a).s_2_sna(s1)
          }
      override def run(s: S): S ** A =
        s_2_sna(s)
    }
  }
