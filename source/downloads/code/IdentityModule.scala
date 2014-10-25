object IdentityModule {
  import demo.FutureModule._
  import demo.SimpleJavaFutureModule._
  import demo.ResModule._
  import demo.TransModule._

  case class Identity[Z](z: Z)

  def identity[Z](z: Z) =
    Identity(z)

  implicit val identityRes =
    new Res[Identity] {
      override def res[Z](block_z: => Z) =
        identity(block_z)
    }

  def identity_trans_future =
    new (Identity ~> Future) {
      def apply[Z](identity_z: Identity[Z]) =
        identity_z match {
          case Identity(z) =>
            mkNow(z)
        }
    }
}
