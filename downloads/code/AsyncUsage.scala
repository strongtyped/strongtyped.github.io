  object AsyncUsage
    extends AsynchronousSourceUsage {
    import Instances.ZeroOrMoreModule
    import Instances.AsyncModule

    override val tpm: ZeroOrMoreModule.type = ZeroOrMoreModule
    override val asm: AsyncModule.type = AsyncModule

    import java.util.concurrent.ExecutorService
    import java.util.concurrent.Executors

    override val in: ExecutorService =
      Executors.newScheduledThreadPool(10);

    override def asynchronous_source_usage() {
      super.asynchronous_source_usage
      in.shutdown()
    }

  }
