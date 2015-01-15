object NowEffectProgramModule {
  def bndProgram[Syntax[+_]: Res](implicit `identitySyntax<=syntax`: IdentitySyntax <= Syntax,
    `consoleSyncPrintSyntax<=syntax`: ConsoleSyncPrintSyntax <= Syntax) = {
    implicit val programRes =
      programSyntaxRes[Syntax]
    import programRes.res
    res(1) bnd { z =>
      res(1) bnd { y =>
        res(z + y) bnd { x =>
          consoleSyncPrint(s"${x}\n")
        }
      }
    }
  }

  def plus[Syntax[+_]: Res](implicit `identitySyntax<=syntax`: IdentitySyntax <= Syntax,
    `consoleSyncPrintSyntax<=syntax`: ConsoleSyncPrintSyntax <= Syntax): Int ** Int => Program[Syntax, Int] = {
    case (z, y) =>
      implicit val programRes =
        programSyntaxRes[Syntax]
      import programRes.res
      res(z + y)
  }

  def andProgram[Syntax[+_]: Res](implicit `identitySyntax<=syntax`: IdentitySyntax <= Syntax,
    `consoleSyncPrintSyntax<=syntax`: ConsoleSyncPrintSyntax <= Syntax): Program[Syntax, Unit] = {
    implicit val programRes =
      programSyntaxRes[Syntax]
    import programRes.res
    (res(1) and res(1)) bnd plus bnd { x =>
      consoleSyncPrint(s"${x}\n")
    }
  }

  type EffectSyntax[+Z] = ({ type Syntax01[+Z] = IdentitySyntax[Z] ++ ConsoleSyncPrintSyntax[Z] })#Syntax01[Z]

  object SyntaxImplicits {
    val `identitySyntax<=effectSyntax`: IdentitySyntax <= EffectSyntax =
      subTransLeft[IdentitySyntax, IdentitySyntax, ConsoleSyncPrintSyntax]

    implicit val identitySyntaxRes: Res[EffectSyntax] =
      new Res[EffectSyntax] {
        def res[Z](z: => Z) =
          `identitySyntax<=effectSyntax`.`~>`(IdentitySyntax(z))
      }
  }

  object MeaningImplicits {
    implicit def `identitySyntax~>nwCmpSemantics`[Semantics[+_]: NwCmp]: IdentitySyntax ~> Semantics =
      `syntax~now~>nwCmpSemantics`[Semantics]
        .asInstanceOf[IdentitySyntax ~> Semantics]
    implicit def `consoleSyncPrintSyntax~>nwCmpSemantics`[Semantics[+_]: NwCmp]: ConsoleSyncPrintSyntax ~> Semantics =
      `syntax~now~>nwCmpSemantics`[Semantics]
        .asInstanceOf[ConsoleSyncPrintSyntax ~> Semantics]
  }

  import SyntaxImplicits._
  import MeaningImplicits._

  implicit val `effectSyntax~>ftrCmpSemantics`: EffectSyntax ~> NowSemantics =
    `identitySyntax~>nwCmpSemantics` ++
      `consoleSyncPrintSyntax~>nwCmpSemantics`

  val bndProgramSyntax =
    bndProgram[EffectSyntax]
  val andProgramSyntax =
    andProgram[EffectSyntax]

  val consoleEffectProgramSyntaxWithNowSemanticsBndMain =
    nwExcCmpNowSemantics.safeExec(bndProgramSyntax.`~>semantics`)

  val consoleEffectProgramSyntaxWithNowSemanticsAndMain =
    nwExcCmpNowSemantics.safeExec(andProgramSyntax.`~>semantics`)

}
