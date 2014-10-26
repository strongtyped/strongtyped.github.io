object SupplierModule {

  import demo.FreeModule.{ Res => _, _ }
  import demo.FutureModule._
  import demo.ResModule._
  import demo.RandomVerboseSleepModule._
  import demo.SubModule._
  import demo.TransModule._

  case class Supplier[Z](u2z: Unit => Z)

  def supplier[Z](block_z: => Z) =
    Supplier {
      case () =>
        block_z
    }

  implicit val supplierRes =
    new Res[Supplier] {
      override def res[Z](block_z: => Z) =
        supplier(block_z)
    }

  def supplier_trans_future =
    new (Supplier ~> Future) {
      def apply[Z](supplier_z: Supplier[Z]) =
        supplier_z match {
          case Supplier(u2z) =>
            mkFuture {
              u2z(())
            }
        }
    }

  def supply[F[_]: Res, Z](block_z: => Z)(implicit supplier_sub_f: Supplier <= F): Free[F, Z] =
    lift {
      supplier {
        block_z
      }
    }
}
