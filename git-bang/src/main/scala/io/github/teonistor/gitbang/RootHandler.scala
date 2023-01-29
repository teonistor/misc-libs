package io.github.teonistor.gitbang

import java.io.File
import java.util.concurrent.Executors.newCachedThreadPool
import scala.annotation.tailrec
import scala.collection.immutable.Queue

class RootHandler(
      root: File,
      runnerFactory: File => Runner,
      investigatorFactory: (Runner,Boolean) => RepoInvestigator,
      situationReportMakerFactory: () => SituationReportMaker
      ) {
  def run(): Unit = {
    val executor = newCachedThreadPool()

    val directories = identifyDirs(Queue(root))
    val runners = directories.map(runnerFactory)
    val investigations = runners
      .map(investigatorFactory(_, false))
      .map(executor.submit(_))
      .map(_.get())

    val situationReportMaker = situationReportMakerFactory()
    println(situationReportMaker.makeMultiple(investigations, directories))

    val commands = (investigations zip directories)
      .map(id => new CommandMaker()(id._1))

    println("The following commands would improve the situation:")
    println((commands zip directories)
      .filter(_._1.nonEmpty)
      .flatMap(cd => List("cd", cd._2.getAbsolutePath) +: cd._1)
      .map(_.map(arg => if (arg contains " ") "'" + arg + "'" else arg)
            .mkString(" "))
      .mkString("\n"))

    Some(IOHelper.ask("Autorun all? (y/n)"))
      .filter(_.trim.toUpperCase == "Y")
      .foreach(_=>
        (commands zip runners)
          .filter(_._1.nonEmpty)
          .foreach(cr =>
            cr._1.foreach(c => cr._2.run(c:_*))))

    executor.shutdown()
  }

  @tailrec
  private def identifyDirs(q: Queue[File], r: List[File] = List.empty): List[File] =
    if (q.isEmpty)
      r.reverse

    else {
      val current = q.head
      val subdirs = current.listFiles(_.isDirectory)

      if (subdirs.exists(_.getName == ".git"))
        identifyDirs(q.tail, current +: r)
      else
        identifyDirs(q.tail ++ subdirs, r)
    }
}
