package io.github.teonistor.gitbang

import java.io.{BufferedReader, InputStreamReader}

class IOHelper {
  private lazy val inBuffer = new BufferedReader(new InputStreamReader(System.in))

  def ask(question: String): String = {
    print(question)
    print(" ")
    inBuffer.readLine()
  }
}
