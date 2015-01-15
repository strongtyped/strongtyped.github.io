object ActiveEffectProgramModule {

  def program[Syntax[+_]: Res](implicit `identitySyntax<=syntax`: IdentitySyntax <= Syntax,
    `consoleSyncPrintSyntax<=syntax`: ConsoleSyncPrintSyntax <= Syntax,
    `consoleReadLineSyntax<=syntax`: ConsoleReadLineSyntax <= Syntax,
    `consoleAsyncPrintlnSyntax<=syntax`: ConsoleAsyncPrintlnSyntax <= Syntax,
    `socketReadLineSyntax<=syntax`: SocketReadLineSyntax <= Syntax,
    `socketPrintlnSyntax<=syntax`: SocketPrintlnSyntax <= Syntax): Program[Syntax, Unit] = {
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
              (socketReadLine(socketHost, readPort) and
                socketReadLine(socketHost, readPort)) bnd { res =>
                  val resultString = res.toString
                  (consoleAsyncPrintln(resultString) and
                    socketPrintln(resultString, socketHost, printPort) and
                    socketPrintln(resultString, socketHost, printPort))
                } end
            }
          }
        }
      }
    }
  }

  type Syntax01[+Z] = ({ type Syntax01[+Z] = IdentitySyntax[Z] ++ ConsoleSyncPrintSyntax[Z] })#Syntax01[Z]
  type Syntax02[+Z] = ({ type Syntax02[+Z] = Syntax01[Z] ++ ConsoleReadLineSyntax[Z] })#Syntax02[Z]
  type Syntax03[+Z] = ({ type Syntax03[+Z] = Syntax02[Z] ++ SocketReadLineSyntax[Z] })#Syntax03[Z]
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
      subTransLeft[IdentitySyntax, Syntax02, SocketReadLineSyntax]

    implicit val `donsoleSyncPrintSyntax<=syntax03`: ConsoleSyncPrintSyntax <= Syntax03 =
      subTransLeft[ConsoleSyncPrintSyntax, Syntax02, SocketReadLineSyntax]

    implicit val `consoleReadLineSyntax<=syntax03`: ConsoleReadLineSyntax <= Syntax03 =
      subTransLeft[ConsoleReadLineSyntax, Syntax02, SocketReadLineSyntax]

    implicit val `socketReadLineSyntax<=syntax03`: SocketReadLineSyntax <= Syntax03 =
      subRight[Syntax02, SocketReadLineSyntax]

    implicit val `identitySyntax<=syntax04`: IdentitySyntax <= Syntax04 =
      subTransLeft[IdentitySyntax, Syntax03, ConsoleAsyncPrintlnSyntax]

    implicit val `consoleSyncPrintSyntax<=syntax04`: ConsoleSyncPrintSyntax <= Syntax04 =
      subTransLeft[ConsoleSyncPrintSyntax, Syntax03, ConsoleAsyncPrintlnSyntax]

    implicit val `consoleReadLineSyntax<=syntax04`: ConsoleReadLineSyntax <= Syntax04 =
      subTransLeft[ConsoleReadLineSyntax, Syntax03, ConsoleAsyncPrintlnSyntax]

    implicit val `socketReadLineSyntax<=syntax04`: SocketReadLineSyntax <= Syntax04 =
      subTransLeft[SocketReadLineSyntax, Syntax03, ConsoleAsyncPrintlnSyntax]

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

    implicit val `socketReadLineSyntax<=effectSyntax`: SocketReadLineSyntax <= EffectSyntax =
      subTransLeft[SocketReadLineSyntax, Syntax04, SocketPrintlnSyntax]

    implicit val `consoleAsyncPrintlnSyntax<=effectSyntax`: ConsoleAsyncPrintlnSyntax <= EffectSyntax =
      subTransLeft[ConsoleAsyncPrintlnSyntax, Syntax04, SocketPrintlnSyntax]

    implicit val `socketPrintlSyntax<=effectSyntax`: SocketPrintlnSyntax <= EffectSyntax =
      subRight[Syntax04, SocketPrintlnSyntax]
  }

  object SemanticsImplicits {
    implicit def `identitySyntax~>ftrCmpSemantics`[Semantics[+_]: ActFtrCmp]: IdentitySyntax ~> Semantics =
      `syntax~now~>actFtrCmpSemantics`[Semantics]
        .asInstanceOf[IdentitySyntax ~> Semantics]

    implicit def `consoleSyncPrintSyntax~>ftrCmpSemantics`[Semantics[+_]: ActFtrCmp]: ConsoleSyncPrintSyntax ~> Semantics =
      `syntax~now~>actFtrCmpSemantics`[Semantics]
        .asInstanceOf[ConsoleSyncPrintSyntax ~> Semantics]

    implicit def `consoleReadLineSyntax~>ftrCmpSemantics`[Semantics[+_]: ActFtrCmp]: ConsoleReadLineSyntax ~> Semantics =
      `syntax~now~>actFtrCmpSemantics`[Semantics]
        .asInstanceOf[ConsoleReadLineSyntax ~> Semantics]

    implicit def `socketReadLineSyntax~>ftrCmpSemantics`[Semantics[+_]: ActFtrCmp]: SocketReadLineSyntax ~> Semantics =
      `syntax~activeFuture~>actFtrCmpSemantics`[Semantics]
        .asInstanceOf[SocketReadLineSyntax ~> Semantics]

    implicit def `consoleAsyncPrintlnSyntax~>ftrCmpSemantics`[Semantics[+_]: ActFtrCmp]: ConsoleAsyncPrintlnSyntax ~> Semantics =
      `syntax~activeFuture~>actFtrCmpSemantics`[Semantics]
        .asInstanceOf[ConsoleAsyncPrintlnSyntax ~> Semantics]

    implicit def `socketPrintlnSyntax~>ftrCmpSemantics`[Semantics[+_]: ActFtrCmp]: SocketPrintlnSyntax ~> Semantics =
      `syntax~activeFuture~>actFtrCmpSemantics`[Semantics]
        .asInstanceOf[SocketPrintlnSyntax ~> Semantics]

  }

  import SyntaxImplicits._
  import SemanticsImplicits._

  implicit val `effectSyntax~>ftrCmpSemantics`: EffectSyntax ~> ActiveFutureSemantics =
    `identitySyntax~>ftrCmpSemantics` ++
      `consoleSyncPrintSyntax~>ftrCmpSemantics` ++
      `consoleReadLineSyntax~>ftrCmpSemantics` ++
      `socketReadLineSyntax~>ftrCmpSemantics` ++
      `consoleAsyncPrintlnSyntax~>ftrCmpSemantics` ++
      `socketPrintlnSyntax~>ftrCmpSemantics`

  val effectProgramSyntax = program[EffectSyntax]
  val activeFutureSemantics = actFtrExcCmpFutureSemantics

  val effectProgramSyntaxWithActiveFutureSemanticsMain =
    activeFutureSemantics.safeExec(effectProgramSyntax.`~>semantics`)

}
