object IdentityProgramModule {
  def bndProgram[Syntax[+_]: Res](implicit `identitySyntax<=syntax`: IdentitySyntax <= Syntax) = {
    implicit val programRes =
      programSyntaxRes[Syntax]
    import programRes.res
    res(1) bnd { z =>
      res(1) bnd { y =>
        res(z + y)
      }
    }
  }

  def plus[Syntax[+_]: Res](implicit `identitySyntax<=syntax`: IdentitySyntax <= Syntax): Int ** Int => Program[Syntax, Int] = {
    case (z, y) =>
      implicit val programRes =
        programSyntaxRes[Syntax]
      import programRes.res
      res(z + y)
  }

  def andProgram[Syntax[+_]: Res](implicit `identitySyntax<=syntax`: IdentitySyntax <= Syntax): Program[Syntax, Int] = {
    implicit val programRes =
      programSyntaxRes[Syntax]
    import programRes.res
    (res(1) and res(1)) bnd plus
  }

  object SyntaxImplicits {
    implicit val identitySyntaxRes: Res[IdentitySyntax] =
      new Res[IdentitySyntax] {
        def res[Z](z: => Z) =
          IdentitySyntax(z)
      }
  }

  object SemanticsImplicits {
    implicit def `identitySyntax~>nwCmpSemantics`[Semantics[+_]: NwCmp]: IdentitySyntax ~> Semantics =
      `syntax~now~>nwCmpSemantics`[Semantics]
        .asInstanceOf[IdentitySyntax ~> Semantics]
  }

  import SyntaxImplicits._
  import SemanticsImplicits._

  val identityProgramSyntaxWithNowSemanticsBndMain =
    nwExcCmpNowSemantics.safeExec(bndProgram[IdentitySyntax].`~>semantics`)

  val identityProgramSyntaxWithNowSemanticsAndMain =
    nwExcCmpNowSemantics.safeExec(andProgram[IdentitySyntax].`~>semantics`)

}
