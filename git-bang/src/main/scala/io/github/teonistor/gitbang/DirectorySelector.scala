package io.github.teonistor.gitbang

import java.io.File
import scala.annotation.tailrec
import scala.collection.immutable.Queue

// TODO Option to recurse further
class DirectorySelector {

  def select(root: File): List[File] =
    select(Queue(root), List.empty)

  @tailrec
  private def select(q: Queue[File], r: List[File]): List[File] =
    if (q.isEmpty)
      r.reverse

    else {
      val current = q.head
      val subdirs = current.listFiles(_.isDirectory)

      if (subdirs.exists(_.getName == ".git"))
        select(q.tail, current +: r)
      else
        select(q.tail ++ subdirs, r)
    }
}
