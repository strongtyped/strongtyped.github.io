  implicit def prg[Syntax[+_]: Res] =
    new Prg[({ type ProgramSyntax[+Z] = Program[Syntax, Z] })#ProgramSyntax] {
      override def res[Z](z: => Z): Program[Syntax, Z] =
        programSyntaxRes.res(z)
      override def bnd[Z, Y](psz: Program[Syntax, Z])(z2psy: Z => Program[Syntax, Y]): Program[Syntax, Y] =
        Bnd_(psz, z2psy)
      override def and[Z, Y](psz: Program[Syntax, Z])(psy: Program[Syntax, Y]): Program[Syntax, Z ** Y] =
        And_(psz, psy)
    }

  class Program[Syntax[+_]: Res, +Z]
    extends Prog[({ type ProgramSyntax[+Z] = Program[Syntax, Z] })#ProgramSyntax, Z]
    with Meaning[Syntax, Z] { psz: Program[Syntax, Z] =>
    override def `~>semantics`[Semantics[+_]: Cmp](implicit `syntax~>semantics`: Syntax ~> Semantics): Semantics[Z] =
      `programSyntax~>semantics`[Syntax, Semantics](`syntax~>semantics`).apply(psz)
  }

  case class Res_[Syntax[+_]: Res, +Z](sz: Syntax[Z])
    extends Program[Syntax, Z]

  private case class Bnd_[Syntax[+_]: Res, +Z, ZZ <: Z, +Y](psz: Program[Syntax, ZZ], z2psy: ZZ => Program[Syntax, Y])
    extends Program[Syntax, Y]

  case class And_[Syntax[+_]: Res, +Z, +Y](psz: Program[Syntax, Z], psy: Program[Syntax, Y])
    extends Program[Syntax, Z ** Y]

  def programSyntaxRes[Syntax[+_]: Res] =
    new Res[({ type ProgramSyntax[+Z] = Program[Syntax, Z] })#ProgramSyntax] {
      def res[Z](z: => Z) =
        Res_(Res[Syntax].res(z))
    }

  def `programSyntax~>semantics`[Syntax[+_]: Res, Semantics[+_]: Cmp](`syntax~>semantics`: Syntax ~> Semantics): ({ type ProgramSyntax[+Z] = Program[Syntax, Z] })#ProgramSyntax ~> Semantics =
    new (({ type ProgramSyntax[+Z] = Program[Syntax, Z] })#ProgramSyntax ~> Semantics) {
      lazy val programSemantics = `programSyntax~>semantics`(`syntax~>semantics`)
      def apply[Z](programSyntax: Program[Syntax, Z]): Semantics[Z] =
        programSyntax match {
          case Res_(sz) =>
            `syntax~>semantics`(sz)
          case Bnd_(psz, z2psy) =>
            Cmp[Semantics].bnd(programSemantics(psz)) { z =>
              programSemantics(z2psy(z))
            }
          case And_(psz, psy) =>
            Cmp[Semantics].and(programSemantics(psz))(programSemantics(psy))
        }
    }

  def `subSyntax~>programSyntax`[SubSyntax[+_], Syntax[+_]: Res](`subSyntax<=syntax`: SubSyntax <= Syntax) =
    new (SubSyntax ~> ({ type ProgramSyntax[+Z] = Program[Syntax, Z] })#ProgramSyntax) {
      def apply[Z](subSyntax: SubSyntax[Z]) =
        Res_(`subSyntax<=syntax`.`~>`(subSyntax))
    }
