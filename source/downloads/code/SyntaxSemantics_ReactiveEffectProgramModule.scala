object ReactiveEffectProgramModule {

  def program[Syntax[+_]: Res, Semantics[+_]: ExcCmp](implicit `identitySyntax<=syntax`: IdentitySyntax <= Syntax,
    `consoleSyncPrintSyntax<=syntax`: ConsoleSyncPrintSyntax <= Syntax,
    `consoleReadLineSyntax<=syntax`: ConsoleReadLineSyntax <= Syntax,
    `consoleAsyncPrintlnSyntax<=syntax`: ConsoleAsyncPrintlnSyntax <= Syntax,
    `socketAsyncReadLineSyntax<=syntax`: SocketAsyncReadLineSyntax <= Syntax,
    `socketPrintlnSyntax<=syntax`: SocketPrintlnSyntax <= Syntax,
    `syntax~>semantics`: Syntax ~> Semantics): Program[Syntax, Unit] = {
    val programRes =
      programSyntaxRes[Syntax]
    import programRes.res
    res("ubuntu-laptop") bnd { socketHost =>
      consoleSyncPrint("read port: ") seq {
        consoleReadLine() bnd { readPortString =>
          val readPort = Integer.parseInt(readPortString)
          consoleSyncPrint("print port: ") seq {
            consoleReadLine() bnd { printPortString =>
              val printPort = Integer.parseInt(printPortString)
              (res(ExcCmp[Semantics].safeExec(
                (socketAsyncReadLine(socketHost, readPort)
                  and socketAsyncReadLine(socketHost, readPort)).`~>semantics`))) bnd {
                case (res1, res2) =>
                  (consoleAsyncPrintln((res1, res2).toString) and
                    socketPrintln(res1.toString, socketHost, printPort) and
                    socketPrintln(res2.toString, socketHost, printPort))
              } end
            }
          }
        }
      }
    }
  }

  type Syntax01[+Z] = ({ type Syntax01[+Z] = IdentitySyntax[Z] ++ ConsoleSyncPrintSyntax[Z] })#Syntax01[Z]
  type Syntax02[+Z] = ({ type Syntax02[+Z] = Syntax01[Z] ++ ConsoleReadLineSyntax[Z] })#Syntax02[Z]
  type Syntax03[+Z] = ({ type Syntax03[+Z] = Syntax02[Z] ++ SocketAsyncReadLineSyntax[Z] })#Syntax03[Z]
  type Syntax04[+Z] = ({ type Syntax04[+Z] = Syntax03[Z] ++ ConsoleAsyncPrintlnSyntax[Z] })#Syntax04[Z]
  type EffectSyntax[+Z] = ({ type Syntax05[+Z] = Syntax04[Z] ++ SocketPrintlnSyntax[Z] })#Syntax05[Z]

  object SyntaxImplicits {

    val `identitySyntax<=syntax01`: IdentitySyntax <= Syntax01 =
      subTransLeft[IdentitySyntax, IdentitySyntax, ConsoleSyncPrintSyntax]

    implicit val `identitySyntax<=syntax02`: IdentitySyntax <= Syntax02 =
      subTransLeft[IdentitySyntax, Syntax01, ConsoleReadLineSyntax]

    implicit val `consoleSyncPrintSyntax<=syntax02`: ConsoleSyncPrintSyntax <= Syntax02 =
      subTransLeft[ConsoleSyncPrintSyntax, Syntax01, ConsoleReadLineSyntax]

    implicit val `consoleReadLineSyntax<=syntax02`: ConsoleReadLineSyntax <= Syntax02 =
      subRight[Syntax01, ConsoleReadLineSyntax]

    implicit val `identitySyntax<=syntax03`: IdentitySyntax <= Syntax03 =
      subTransLeft[IdentitySyntax, Syntax02, SocketAsyncReadLineSyntax]

    implicit val `donsoleSyncPrintSyntax<=syntax03`: ConsoleSyncPrintSyntax <= Syntax03 =
      subTransLeft[ConsoleSyncPrintSyntax, Syntax02, SocketAsyncReadLineSyntax]

    implicit val `consoleReadLineSyntax<=syntax03`: ConsoleReadLineSyntax <= Syntax03 =
      subTransLeft[ConsoleReadLineSyntax, Syntax02, SocketAsyncReadLineSyntax]

    implicit val `fileAsyncReadLineSyntax<=syntax03`: SocketAsyncReadLineSyntax <= Syntax03 =
      subRight[Syntax02, SocketAsyncReadLineSyntax]

    implicit val `identitySyntax<=syntax04`: IdentitySyntax <= Syntax04 =
      subTransLeft[IdentitySyntax, Syntax03, ConsoleAsyncPrintlnSyntax]

    implicit val `consoleSyncPrintSyntax<=syntax04`: ConsoleSyncPrintSyntax <= Syntax04 =
      subTransLeft[ConsoleSyncPrintSyntax, Syntax03, ConsoleAsyncPrintlnSyntax]

    implicit val `consoleReadLineSyntax<=syntax04`: ConsoleReadLineSyntax <= Syntax04 =
      subTransLeft[ConsoleReadLineSyntax, Syntax03, ConsoleAsyncPrintlnSyntax]

    implicit val `fileAsyncReadLineSyntax<=syntax04`: SocketAsyncReadLineSyntax <= Syntax04 =
      subTransLeft[SocketAsyncReadLineSyntax, Syntax03, ConsoleAsyncPrintlnSyntax]

    implicit val `consoleAsyncPrintlnSyntax<=syntax04`: ConsoleAsyncPrintlnSyntax <= Syntax04 =
      subRight[Syntax03, ConsoleAsyncPrintlnSyntax]

    implicit val `identitySyntax<=effectSyntax`: IdentitySyntax <= EffectSyntax =
      subTransLeft[IdentitySyntax, Syntax04, SocketPrintlnSyntax]

    implicit val effectSyntaxRes: Res[EffectSyntax] =
      new Res[EffectSyntax] {
        def res[Z](z: => Z) =
          `identitySyntax<=effectSyntax`.`~>`(IdentitySyntax(z))
      }

    implicit val `consoleSyncPrintSyntax<=effectSyntax`: ConsoleSyncPrintSyntax <= EffectSyntax =
      subTransLeft[ConsoleSyncPrintSyntax, Syntax04, SocketPrintlnSyntax]

    implicit val `consoleReadLineSyntax<=effectSyntax`: ConsoleReadLineSyntax <= EffectSyntax =
      subTransLeft[ConsoleReadLineSyntax, Syntax04, SocketPrintlnSyntax]

    implicit val `fileAsyncReadLineSyntax<=effectSyntax`: SocketAsyncReadLineSyntax <= EffectSyntax =
      subTransLeft[SocketAsyncReadLineSyntax, Syntax04, SocketPrintlnSyntax]

    implicit val `consoleAsyncPrintlnSyntax<=effectSyntax`: ConsoleAsyncPrintlnSyntax <= EffectSyntax =
      subTransLeft[ConsoleAsyncPrintlnSyntax, Syntax04, SocketPrintlnSyntax]

    implicit val `socketPrintlSyntax<=effectSyntax`: SocketPrintlnSyntax <= EffectSyntax =
      subRight[Syntax04, SocketPrintlnSyntax]
  }

  object SemanticsImplicits {
    implicit def `identitySyntax~>rctFtrCmpSemantics`[Semantics[+_]: RctFtrCmp]: IdentitySyntax ~> Semantics =
      `syntax~now~>rctFtrCmpSemantics`[Semantics]
        .asInstanceOf[IdentitySyntax ~> Semantics]

    implicit def `consoleSyncPrintSyntax~>rctFtrCmpSemantics`[Semantics[+_]: RctFtrCmp]: ConsoleSyncPrintSyntax ~> Semantics =
      `syntax~now~>rctFtrCmpSemantics`[Semantics]
        .asInstanceOf[ConsoleSyncPrintSyntax ~> Semantics]

    implicit def `consoleReadLineSyntax~>rctFtrCmpSemantics`[Semantics[+_]: RctFtrCmp]: ConsoleReadLineSyntax ~> Semantics =
      `syntax~now~>rctFtrCmpSemantics`[Semantics]
        .asInstanceOf[ConsoleReadLineSyntax ~> Semantics]

    implicit def `socketAsyncReadLineSyntax~>rctFtrCmpSemantics`[Semantics[+_]: RctFtrCmp]: SocketAsyncReadLineSyntax ~> Semantics =
      `syntax~reactiveFuture~>rctFtrCmpSemantics`[Semantics]
        .asInstanceOf[SocketAsyncReadLineSyntax ~> Semantics]

    implicit def `consoleAsyncPrintlnSyntax~>rctFtrCmpSemantics`[Semantics[+_]: RctFtrCmp]: ConsoleAsyncPrintlnSyntax ~> Semantics =
      `syntax~activeFuture~>rctFtrCmpSemantics`[Semantics]
        .asInstanceOf[ConsoleAsyncPrintlnSyntax ~> Semantics]

    implicit def `socketPrintlnSyntax~>rctFtrCmpSemantics`[Semantics[+_]: RctFtrCmp]: SocketPrintlnSyntax ~> Semantics =
      `syntax~activeFuture~>rctFtrCmpSemantics`[Semantics]
        .asInstanceOf[SocketPrintlnSyntax ~> Semantics]

  }

  import SyntaxImplicits._
  import SemanticsImplicits._

  import ReactiveFutureSemanticsModule._

  implicit val `effectSyntax~>rctFtrCmpSemantics`: EffectSyntax ~> ReactiveFutureSemantics =
    `identitySyntax~>rctFtrCmpSemantics` ++
      `consoleSyncPrintSyntax~>rctFtrCmpSemantics` ++
      `consoleReadLineSyntax~>rctFtrCmpSemantics` ++
      `socketAsyncReadLineSyntax~>rctFtrCmpSemantics` ++
      `consoleAsyncPrintlnSyntax~>rctFtrCmpSemantics` ++
      `socketPrintlnSyntax~>rctFtrCmpSemantics`

  val effectProgramSyntax = program[EffectSyntax, ReactiveFutureSemantics]
  val reactiveFutureSemantics = rctFtrExcCmpFutureSemantics

  val effectProgramSyntaxWithReactiveFutureSemanticsMain =
    reactiveFutureSemantics.safeExec(effectProgramSyntax.`~>semantics`)

}
