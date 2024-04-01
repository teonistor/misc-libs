package io.github.teonistor.gitbang

import java.io.{BufferedReader, File, InputStreamReader}
import java.lang.ProcessBuilder.Redirect.INHERIT
import scala.jdk.CollectionConverters.IteratorHasAsScala
import scala.util.Using

class Runner (dir: File) {

  def run(command: String*):String = {
    val process = new ProcessBuilder(command: _*)
      .directory(dir)
      .redirectInput(INHERIT)
      .redirectError(INHERIT)
      .start()

    val output = Using(new BufferedReader(new InputStreamReader(process.getInputStream)))(_
      .lines().iterator().asScala.mkString("\n"))

    println(output)
    println("Exit " + process.waitFor())
    output.get
  }
}
