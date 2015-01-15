object StillPureProgramImpurePureMain {

  private def bndMain() {
    val writer = System.console.writer()
    writer.println(identityProgramSyntaxWithNowSemanticsBndMain)
    writer.flush
  }
  private def andMain() {
    val writer = System.console.writer()
    writer.println(identityProgramSyntaxWithNowSemanticsAndMain)
    writer.flush
  }

  def main(args: Array[String]): Unit = {
    bndMain()
    andMain()
  }

}
