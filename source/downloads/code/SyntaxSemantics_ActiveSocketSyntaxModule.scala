object ActiveSocketSyntaxModule {

  type SocketHost = String
  type SocketPort = Int

  case class SocketReadLineSyntax[+Z](socketHost: SocketHost, socketPort: SocketPort)
    extends Syntax[String] {
    override def active = {
      val socket: java.net.Socket =
        new java.net.Socket(socketHost, socketPort)
      val string: String =
        new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine()
      socket.close()
      string
    }
  }

  case class SocketPrintlnSyntax[+Z](string: String, socketHost: SocketHost, socketPort: SocketPort)
    extends Syntax[Unit] {
    override def active = {
      val socket: java.net.Socket =
        new java.net.Socket(socketHost, socketPort)
      val unit: Unit =
        new PrintWriter(socket.getOutputStream(), true).println(string)
      socket.close()
      unit
    }
  }

  def socketReadLine[Syntax[+_]: Res](socketHost: SocketHost, socketPort: SocketPort)(implicit `socketReadLineSyntax<=syntax`: SocketReadLineSyntax <= Syntax): Program[Syntax, String] =
    `subSyntax~>programSyntax`(`socketReadLineSyntax<=syntax`).apply(SocketReadLineSyntax(socketHost, socketPort))

  def socketPrintln[Syntax[+_]: Res](string: String, socketHost: SocketHost, socketPort: SocketPort)(implicit `socketPrintlnSyntax<=syntax`: SocketPrintlnSyntax <= Syntax): Program[Syntax, Unit] =
    `subSyntax~>programSyntax`(`socketPrintlnSyntax<=syntax`).apply(SocketPrintlnSyntax(string, socketHost, socketPort))

}
