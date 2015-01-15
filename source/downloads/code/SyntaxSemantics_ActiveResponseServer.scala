class ActiveResponseServerThread(i: Int, socket: Socket) extends Thread {
  override def run() {
    println("Client connected...");
    val printWriter =
      new PrintWriter(socket.getOutputStream(), true)
    randomVerboseSleep(s"sending response to ${i}")(2000) // (3000)
    val response =
      s"Hello - ${i}"
    printWriter.println(response)
    socket.close()
  }
}

object ActiveResponseServer {
  def main(args: Array[String]) {
    val port =
      4321
    val serverSocket = new ServerSocket(port)
    var i =
      1
    println("Active response server bound to port: " + serverSocket.getLocalPort());
    while (true) {
      println("Waiting for clients to connect...");
      val socket =
        serverSocket.accept()
      new ActiveRequestServerThread(i, socket).start();
      i =
        i + 1
    }
  }
}
