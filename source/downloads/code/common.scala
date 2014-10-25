    import java.io.File

    import RandomVerboseSleepModule._
    import demo.EffectModule._
    import demo.FreeModule.{ Res => _, _ }
    import demo.FutureModule._
    import demo.IdentityModule.{ identityRes => _, _ }
    import demo.ProductModule._
    import demo.ResModule._
    import demo.SubModule._
    import demo.SumModule._
    import demo.SupplierModule.{ supplierRes => _, _ }
    import demo.WithFileModule.{ withFileRes => _, _ }

    import java.util.concurrent.{ ExecutorService, Executors }

    val es: ExecutorService =
      Executors.newScheduledThreadPool(8);

    val path = "/opt/home/data/sbt/javaOneUniversity/src/main/scala/demo/"

    val fooFile =
      new File(path + "foo")

    val barFile =
      new File(path + "bar")
