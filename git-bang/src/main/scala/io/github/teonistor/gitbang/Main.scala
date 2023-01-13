package io.github.teonistor.gitbang

import java.io.File

object Main {
  def main(arg: Array[String]): Unit = {

    val root = arg.headOption
      .getOrElse(IOHelper.ask("Root directory not provided as argument. Please enter now:"))

    new RootHandler(
        new File(root),
        new Runner(_),
        new RepoInvestigator(_,_))
      .run()
  }
}
