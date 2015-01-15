  type Handler[-Z] = Z => Unit

  trait Syntax[+Z] {
    def active: Z
    def reactive(hz: Handler[Z]): Unit =
      hz(active)
  }
