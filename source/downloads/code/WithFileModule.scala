object WithFileModule {

  import java.io.File

  import demo.FreeModule.{ Res => _, _ }
  import demo.FutureModule._
  import demo.ResModule._
  import demo.SleepModule._
  import demo.SubModule._
  import demo.TransModule._

  import scala.io.Source

  case class WithFile[Z](of: Option[File], f2z: File => Z)

  implicit val withFileRes =
    new Res[WithFile] {
      override def res[Z](z: => Z) =
        WithFile(None, {
          _ =>
            z
        })
    }

  def withFile_trans_future(what: String)(speed: Int) =
    new (WithFile ~> Future) {
      def apply[Z](withFile: WithFile[Z]) =
        withFile match {
          case WithFile(of, f2z) =>
            mkFuture {
              randomVerboseSleep(what)(speed)
              f2z(of.get)
            }
        }
    }

  def withFile[F[_]: Res, Z](file: File)(f2z: File => Z)(implicit withFile_sub_f: WithFile <= F): Free[F, Z] =
    lift(WithFile(Some(file), { file =>
      println(s"readFile ${file.getName()}")
      f2z(file)
    }))

  def readFile[F[_]: Res](file: File)(implicit withFile_sub_f: WithFile <= F): Free[F, String] =
    withFile(file)(Source.fromFile(_).mkString)

}
