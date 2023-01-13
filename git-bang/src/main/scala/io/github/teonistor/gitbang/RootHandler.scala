package io.github.teonistor.gitbang

import java.io.File
import java.util.concurrent.Executors.newCachedThreadPool
import scala.annotation.tailrec
import scala.collection.immutable.Queue

class RootHandler(
      root: File,
      runnerFactory: File => Runner,
      investigatorFactory: (Runner,Boolean) => RepoInvestigator) {
  def run(): Unit = {
    val executor = newCachedThreadPool()

    val directories = identifyDirs(Queue(root))
    val runners = directories.map(runnerFactory)
    val investigations = runners
      .map(investigatorFactory(_, false))
      .map(executor.submit(_))
      .map(_.get())

    println(" ------- Git Situation Report -------")
    (investigations zip directories).foreach(id => {
      println(id._2)
      println("Remote production branch: " + id._1.remoteProductionBranch.getOrElse("None"))
      println(id._1.checkedoutBranch.map("Checked out on " +_).getOrElse("Not checked out"))
      if (id._1.toDelete.nonEmpty)
        println(id._1.toDelete.mkString("The following local branches will be deleted:\n  ", "\n  ", ""))
      if (id._1.toKeep.nonEmpty)
        println(id._1.toKeep.mkString("The following local branches will be kept:\n  ", "\n  ", ""))
      println(" ------------------------------------")
    })

    val commands = (investigations zip directories)
      .map(id => new CommandMaker(id._2)(id._1))

    println("The following commands would improve the situation:")
    println(commands
      .filter(_.size > 1)
      .flatten
      .map(_.map(arg => if (arg contains " ") "'" + arg + "'" else arg)
            .mkString(" "))
      .mkString("\n"))

//    Some(IOHelper.ask("Autorun all? (y/n)"))
//      .filter(_.trim.toUpperCase == "Y")
//      .foreach(_=> {
//        println("Yes but no")
//      })

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
