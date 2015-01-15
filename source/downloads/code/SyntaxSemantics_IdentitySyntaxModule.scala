object IdentitySyntaxModule {

  case class IdentitySyntax[+Z](z: Z)
    extends Syntax[Z] {
    def active = z
  }

  val identitySyntaxRes =
    new Res[IdentitySyntax] {
      def res[Z](z: => Z) =
        IdentitySyntax(z)
    }

  def identity[Syntax[+_]: Res, Z](z: Z)(implicit `identitySyntax<=syntax`: IdentitySyntax <= Syntax): Program[Syntax, Z] =
    `subSyntax~>programSyntax`(`identitySyntax<=syntax`).apply(IdentitySyntax(z))

}
