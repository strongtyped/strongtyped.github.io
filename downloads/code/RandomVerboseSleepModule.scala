object RandomVerboseSleepModule {
  import java.lang.Thread.sleep

  def show(what: String)(i: Int) {
    println(s"${what} ${i}")
  }

  import scala.math._

  def randomSleep(speed: Int) =
    sleep(round(speed * random))

  def randomVerboseSleep(what: String)(speed: Int) =
    (1 to 5).toList.foreach {
      i =>
        randomSleep(speed)
        show(what)(i)
    }
}
