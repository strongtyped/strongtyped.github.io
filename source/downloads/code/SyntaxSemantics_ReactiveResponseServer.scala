class ReactiveResponseServerThread(i: Int, clientChannel: AsynchronousSocketChannel)
  extends Thread {
  override def run() {
    println("Client connected...");
    randomVerboseSleep(s"sending response to ${i}")(2000)
    val response =
      s"Hello - ${i}"
    val byteBuffer =
      ByteBuffer.wrap(response.getBytes())
    clientChannel.write(byteBuffer)
    byteBuffer.clear();
    clientChannel.close()
  }
}

object ReactiveResponseServer {
  def main(args: Array[String]) {
    val asynchronousServerSocketChannel =
      AsynchronousServerSocketChannel.open();
    val inetSocketAddress =
      new InetSocketAddress("ubuntu-laptop", 4321);
    asynchronousServerSocketChannel.bind(inetSocketAddress);
    println("Reactive rsponse server channel bound to port: " + inetSocketAddress.getPort());
    var i =
      1
    while (true) {
      println("Waiting for clients to connect...");
      val acceptResult =
        asynchronousServerSocketChannel.accept();
      val clientChannel =
        acceptResult.get();
      new ReactiveResponseServerThread(i, clientChannel).start();
      i =
        i + 1
    }
  }
}
