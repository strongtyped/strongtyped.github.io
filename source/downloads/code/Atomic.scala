  class Atomic[Z] {
    var z: Z = _
    val countDownLatch: CountDownLatch = new CountDownLatch(1)
    def getValue(timeOut: TimeOut) = {
      countDownLatch.await(timeOut, MILLISECONDS)
      z
    }
    def setValue(newZ: Z) {
      z = newZ
      countDownLatch.countDown()
    }
  }
