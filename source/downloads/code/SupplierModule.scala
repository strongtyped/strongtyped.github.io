object SupplierModule {

  import demo.FreeModule.{ Res => _, _ }
  import demo.FutureModule._
  import demo.ResModule._
  import demo.SleepModule._
  import demo.SubModule._
  import demo.TransModule._

  case class Supplier[Z](u2a: Unit => Z)

  implicit val supplierRes =
    new Res[Supplier] {
      override def res[Z](z: => Z) =
        Supplier {
          case () =>
            z
        }
    }

  def supplier_trans_future(what: String)(speed: Int) =
    new (Supplier ~> Future) {
      def apply[Z](supplier: Supplier[Z]) =
        supplier match {
          case Supplier(u2z) =>
            mkFuture {
              randomVerboseSleep(what)(speed)
              u2z(())
            }
        }
    }

  def supply[F[_]: Res](x: Int)(implicit supplier_sub_f: Supplier <= F): Free[F, Int] =
    lift(Supplier({
      case () =>
        println(s"supplied ${x}")
        x
    }))
}
