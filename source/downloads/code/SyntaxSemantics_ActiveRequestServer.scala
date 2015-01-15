class ActiveRequestServerThread(i: Int, socket: Socket)
  extends Thread {
  override def run() {
    println("Client connected...");
    val bufferedReader =
      new BufferedReader(new InputStreamReader(socket.getInputStream()));
    val request =
      bufferedReader.readLine()
    randomVerboseSleep(s"receiving ${request} from ${i}")(2000)
    socket.close()
  }
}

object ActiveRequestServer {
  def main(args: Array[String]) {
    val port =
      1234
    val serverSocket =
      new ServerSocket(port)
    var i =
      1
    println("Active request server bound to port: " + serverSocket.getLocalPort());
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
