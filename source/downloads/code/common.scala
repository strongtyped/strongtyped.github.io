    import java.io.File

    import demo.EffectModule._
    import demo.FreeModule._
    import demo.FutureModule._
    import demo.IdentityModule.{ identityResult => _, _ }
    import demo.ProductModule._
    import demo.ResultModule._
    import demo.SubModule._
    import demo.SumModule._
    import demo.SupplierModule.{ supplierResult => _, _ }
    import demo.WithFileModule.{ withFileResult => _, _ }

    import java.util.concurrent.{ ExecutorService, Executors }

    val es: ExecutorService =
      Executors.newScheduledThreadPool(8);

    val path = "/opt/home/data/sbt/javaOneUniversity/src/main/scala/demo/"

    val fooFile =
      new File(path + "foo")

    val barFile =
      new File(path + "bar")
