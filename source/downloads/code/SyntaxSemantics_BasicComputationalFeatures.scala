  trait Res[R[+_]] {
    def res[Z](z: => Z): R[Z]
  }

  object Res {
    def apply[R[+_]: Res] = implicitly[Res[R]]
  }

  trait Bnd[B[+_]] {
    def bnd[Z, Y](bz: B[Z])(z2by: Z => B[Y]): B[Y]
    def seq[Z, Y](bz: B[Z])(by: B[Y]): B[Y] =
      bnd(bz) { _ =>
        by
      }
  }

  object Bnd {
    def apply[B[+_]: Bnd] = implicitly[Bnd[B]]
  }

  trait And[A[+_]] {
    def and[Z, Y](az: A[Z])(ay: A[Y]): A[Z ** Y]
  }

  object And {
    def apply[A[+_]: And] = implicitly[And[A]]
  }

  trait ResourceFactory {
    type Rsc[+Z] <: Resource[Z]
    def acquire[Z](): Rsc[Z]
    def safe[Z, Y](exec: Rsc[Z] => Y): Y = {
      val rz = acquire[Z]()
      val y = exec(rz)
      rz.release()
      y
    }
    trait Resource[+A] {
      def release(): Unit
    }
  }

  trait Exc[E[+_]] {
    type Result[+Z]
    type RscFac <: ResourceFactory
    val rscFac: RscFac
    private[strongtyped] def exec[Z](ez: E[Z])(rscz: RscFac#Rsc[Z]): Result[Z]
    def safeExec[Z](ez: E[Z]): Result[Z] =
      rscFac.safe(exec(ez))
  }
