  trait Nw[N[+_]] {
    def now[Z](z: => Z): N[Z]
  }

  object Nw {
    def apply[N[+_]: Nw] = implicitly[Nw[N]]
  }

  trait NwCmp[NC[+_]]
    extends Nw[NC]
    with Cmp[NC]

  object NwCmp {
    def apply[NC[+_]: NwCmp] = implicitly[NwCmp[NC]]
  }

  def `syntax~now~>nwCmpSemantics`[Semantics[+_]: NwCmp] =
    new (Syntax ~> Semantics) {
      def apply[Z](syntax: Syntax[Z]) =
        NwCmp[Semantics].now(syntax.active)
    }

  trait NwExcCmp[NEC[+_]]
    extends NwCmp[NEC]
    with Exc[NEC]

  object NwExcCmp {
    def apply[NEC[+_]: NwExcCmp] = implicitly[NwExcCmp[NEC]]
  }

  trait ActFtr[AF[+_]]
    extends Nw[AF] {
    def activeFuture[Z](z: => Z): AF[Z]
  }

  object ActFtr {
    def apply[AF[+_]: ActFtr] = implicitly[ActFtr[AF]]
  }

  trait ActFtrCmp[AFC[+_]]
    extends ActFtr[AFC]
    with Cmp[AFC]

  object ActFtrCmp {
    def apply[AFC[+_]: ActFtrCmp] = implicitly[ActFtrCmp[AFC]]
  }

  def `syntax~now~>actFtrCmpSemantics`[Semantics[+_]: ActFtrCmp] =
    new (Syntax ~> Semantics) {
      def apply[Z](syntax: Syntax[Z]) =
        ActFtrCmp[Semantics].now(syntax.active)
    }

  def `syntax~activeFuture~>actFtrCmpSemantics`[Semantics[+_]: ActFtrCmp] =
    new (Syntax ~> Semantics) {
      def apply[Z](syntax: Syntax[Z]) =
        ActFtrCmp[Semantics].activeFuture(syntax.active)
    }

  trait ActFtrExcCmp[AFEC[+_]]
    extends ActFtrCmp[AFEC]
    with Exc[AFEC]

  object ActFtrExcCmp {
    def apply[AFEC[+_]: ActFtrExcCmp] = implicitly[ActFtrExcCmp[AFEC]]
  }

  trait RctFtr[RF[+_]]
    extends ActFtr[RF] {
    def reactiveFuture[Z](call: Handler[Z] => Unit): RF[Z]
    override def activeFuture[Z](z: => Z): RF[Z] =
      reactiveFuture[Z] { hz =>
        hz(z)
      }
  }

  object RctFtr {
    def apply[RF[+_]: RctFtr] = implicitly[RctFtr[RF]]
  }

  trait RctFtrCmp[RFC[+_]]
    extends RctFtr[RFC]
    with Cmp[RFC]

  object RctFtrCmp {
    def apply[RFC[+_]: RctFtrCmp] = implicitly[RctFtrCmp[RFC]]
  }

  def `syntax~now~>rctFtrCmpSemantics`[Semantics[+_]: RctFtrCmp] =
    new (Syntax ~> Semantics) {
      def apply[Z](syntax: Syntax[Z]) =
        RctFtrCmp[Semantics].now(syntax.active)
    }

  def `syntax~activeFuture~>rctFtrCmpSemantics`[Semantics[+_]: RctFtrCmp] =
    new (Syntax ~> Semantics) {
      def apply[Z](syntax: Syntax[Z]) =
        RctFtrCmp[Semantics].activeFuture(syntax.active)
    }

  def `syntax~reactiveFuture~>rctFtrCmpSemantics`[Semantics[+_]: RctFtrCmp] =
    new (Syntax ~> Semantics) {
      def apply[Z](syntax: Syntax[Z]) = {
        RctFtrCmp[Semantics].reactiveFuture(syntax.reactive)
      }
    }

  trait RctFtrExcCmp[RFEC[+_]]
    extends RctFtrCmp[RFEC]
    with ExcCmp[RFEC]

  object RctFtrExcCmp {
    def apply[RFEC[+_]: RctFtrExcCmp] = implicitly[RctFtrExcCmp[RFEC]]
  }
