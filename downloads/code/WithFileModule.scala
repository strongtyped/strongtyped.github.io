object WithFileModule {

  import java.io.File

  import demo.FreeModule.{ Res => _, _ }
  import demo.FutureModule._
  import demo.ResModule._
  import demo.RandomVerboseSleepModule._
  import demo.SubModule._
  import demo.TransModule._

  import scala.io.Source

  type FilePath = String

  case class WithFile[Z](filePath: FilePath, file2z: File => Z)

  def withFile[Z](filePath: FilePath)(file2z: File => Z) =
    WithFile(filePath, file2z)

  implicit val withFileRes =
    new Res[WithFile] {
      override def res[Z](block_z: => Z) =
        withFile("") { _ =>
          block_z
        }
    }

  def withFile_trans_future =
    new (WithFile ~> Future) {
      def apply[Z](withFile: WithFile[Z]) =
        withFile match {
          case WithFile(filePath, file2z) =>
            mkFuture {
              randomVerboseSleep("withFile")(1500)
              file2z(new File(filePath))
            }
        }
    }

  def readFile[F[_]: Res](filePath: FilePath)(implicit withFile_sub_f: WithFile <= F): Free[F, String] =
    lift {
      withFile(filePath) { file =>
        randomVerboseSleep("readFile")(1500)
        val content = Source.fromFile(file).mkString
        content.substring(0, content.length() - 1)
      }
    }

}
