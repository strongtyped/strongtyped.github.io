object IdentityModule {
  import demo.FutureModule._
  import demo.JavaFutureModule._
  import demo.ResultModule._
  import demo.TransModule._
  import demo.Util.show

  case class Identity[Z](z: Z)

  implicit val identityResult =
    new Result[Identity] {
      override def res[Z](z: => Z) =
        Identity(z)
    }

  def identity_trans_future(what: String)(i: Int) =
    new (Identity ~> Future) {
      def apply[Z](id: Identity[Z]) =
        id match {
          case Identity(z) =>
            _ =>
              (_: TimeOut) => {
                show(what)(i)
                z
              }
        }
    }
}
