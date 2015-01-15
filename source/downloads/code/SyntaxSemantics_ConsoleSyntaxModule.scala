object ConsoleSyntaxModule {

  case class ConsoleReadLineSyntax[+Z]()
    extends Syntax[String] {
    def active =
      System.console.readLine
  }

  case class ConsoleSyncPrintSyntax[+Z](string: String)
    extends Syntax[Unit] {
    def active = {
      val writer = System.console.writer()
      writer.print(string)
      writer.flush
    }
  }

  case class ConsoleAsyncPrintlnSyntax[+Z](string: String)
    extends Syntax[Unit] {
    def active = {
      val writer = System.console.writer()
      randomVerboseSleep(s"writeln to console")(2000)
      writer.println(string)
      writer.flush
    }
  }

  def consoleReadLine[Syntax[+_]: Res]()(implicit `consoleReadLineSyntax<=syntax`: ConsoleReadLineSyntax <= Syntax): Program[Syntax, String] =
    `subSyntax~>programSyntax`(`consoleReadLineSyntax<=syntax`).apply(ConsoleReadLineSyntax())

  def consoleSyncPrint[Syntax[+_]: Res](string: String)(implicit `consoleSyncPrintSyntax<=syntax`: ConsoleSyncPrintSyntax <= Syntax): Program[Syntax, Unit] =
    `subSyntax~>programSyntax`(`consoleSyncPrintSyntax<=syntax`).apply(ConsoleSyncPrintSyntax(string))

  def consoleAsyncPrintln[Syntax[+_]: Res](string: String)(implicit `consoleAsyncPrintlnSyntax<=syntax`: ConsoleAsyncPrintlnSyntax <= Syntax): Program[Syntax, Unit] =
    `subSyntax~>programSyntax`(`consoleAsyncPrintlnSyntax<=syntax`).apply(ConsoleAsyncPrintlnSyntax(string))

}
