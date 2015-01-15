  trait Meaning[Syntax[+_], +Z] {
    def `~>semantics`[Semantics[+_]: Cmp](implicit `syntax~>semantics`: Syntax ~> Semantics): Semantics[Z]
  }
