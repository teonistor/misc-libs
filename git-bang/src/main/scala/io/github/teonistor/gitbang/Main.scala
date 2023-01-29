package io.github.teonistor.gitbang

import java.io.File

object Main {
  def main(arg: Array[String]): Unit = {

    val ioHelper = new IOHelper()
    val root = arg.headOption
      .getOrElse(ioHelper.ask("Root directory not provided as argument. Please enter now:"))

    new RootHandler(
        new DirectorySelector(),
        new Runner(_),
        new RepoInvestigator(_,_),
        new CommandMaker,
        new SituationReportMaker(),
        ioHelper)
      .run(new File(root))
  }
}
