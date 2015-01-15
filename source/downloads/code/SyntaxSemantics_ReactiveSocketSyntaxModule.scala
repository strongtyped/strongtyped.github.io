object ReactiveSocketSyntaxModule {

  case class SocketAsyncReadLineSyntax[+Z](socketHost: SocketHost, socketPort: SocketPort)
    extends Syntax[String] {
    override def active = {
      val socket: java.net.Socket =
        new java.net.Socket(socketHost, socketPort)
      val string: String =
        new BufferedReader(new InputStreamReader(socket.getInputStream())).readLine()
      socket.close()
      string
    }
    override def reactive(hString: Handler[String]): Unit = {
      val socketChannel =
        AsynchronousSocketChannel.open()
      socketChannel.connect(new InetSocketAddress(socketHost, socketPort)).get()
      val byteBuffer =
        ByteBuffer.allocate(1024)
      socketChannel.read(byteBuffer, 100, SECONDS, (), new CompletionHandler[Integer, Unit] {
        def completed(bytesRead: Integer, unit: Unit) = { 
          val string =
            new String(byteBuffer.array()).trim()
          hString(string)
        }
        def failed(err: Throwable, ignore: Unit) =
          throw err
      })
    }
  }

  def socketAsyncReadLine[Syntax[+_]: Res](socketHost: SocketHost, socketPort: SocketPort)(implicit `socketAsyncReadLineSyntax<=syntax`: SocketAsyncReadLineSyntax <= Syntax): Program[Syntax, String] =
    `subSyntax~>programSyntax`(`socketAsyncReadLineSyntax<=syntax`).apply(SocketAsyncReadLineSyntax(socketHost, socketPort))
    
}
