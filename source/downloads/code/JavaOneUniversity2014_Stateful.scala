  trait StatefulModule {
    type S
    type D[Z] <: Stateful[Z]
    def get: Unit => D[S]
    def set: S => D[Unit]
    def exec: (S => S) => D[Unit]
    trait Stateful[A] {
    }
  }
