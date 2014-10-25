object WithFileModule {

  import java.io.File

  import demo.FreeModule.{ Res => _, _ }
  import demo.FutureModule._
  import demo.ResModule._
  import demo.RandomVerboseSleepModule._
  import demo.SubModule._
  import demo.TransModule._

  import scala.io.Source

  case class WithFile[Z](of: Option[File], f2z: File => Z)

  def withFile[Z](of: Option[File])(f2z: File => Z) =
    WithFile(of, f2z)

  implicit val withFileRes =
    new Res[WithFile] {
      override def res[Z](block_z: => Z) =
        withFile(None) { _ =>
          block_z
        }
    }

  def withFile_trans_future =
    new (WithFile ~> Future) {
      def apply[Z](withFile: WithFile[Z]) =
        withFile match {
          case WithFile(of, f2z) =>
            mkFuture {
              randomVerboseSleep("withFile")(1500)
              f2z(of.get)
            }
        }
    }

  def readFile[F[_]: Res](file: File)(implicit withFile_sub_f: WithFile <= F): Free[F, String] =
    lift(withFile(Some(file)) { file =>
      randomVerboseSleep("readFile")(1500)
      Source.fromFile(file).mkString
    })

}
