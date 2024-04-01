package io.github.teonistor.gitbang

import java.io.File
import java.util.concurrent.Executors.newCachedThreadPool

class RootHandler(
      directorySelector: DirectorySelector,
      runnerFactory: File => Runner,
      investigatorFactory: (Runner,Boolean) => RepoInvestigator,
      commandMaker: CommandMaker,
      situationReportMaker: SituationReportMaker,
      ioHelper: IOHelper) {
  def run(root: File): Unit = {
    val executor = newCachedThreadPool()

    val directories = directorySelector.select(root)
    val runners = directories.map(runnerFactory)
    val investigations = runners
      .map(investigatorFactory(_, false))
      .map(executor.submit(_))
      .map(_.get())
    val commands = investigations.map(commandMaker)

    println(situationReportMaker.makeMultiple(investigations, directories))
    println("The following commands would improve the situation:")
    println((commands zip directories)
      .filter(_._1.nonEmpty)
      .flatMap(cd => List("cd", cd._2.getAbsolutePath) +: cd._1)
      .map(_.map(arg => if (arg contains " ") "'" + arg + "'" else arg)
            .mkString(" "))
      .mkString("\n"))

    Some(ioHelper.ask("Autorun all? (y/n)"))
      .filter(_.trim.toUpperCase == "Y")
      .foreach(_=>
        (commands zip runners)
          .filter(_._1.nonEmpty)
          .foreach(cr =>
            cr._1.foreach(c => cr._2.run(c:_*))))

    executor.shutdown()
  }
}
