package io.github.teonistor.gitbang

import org.scalatest.funsuite.AnyFunSuite

import java.io.File

class DirectorySelectorTest extends AnyFunSuite {

  test("select")  {

    new File("target/DirectorySelectorTest/a/.git/dummy").mkdirs()
    new File("target/DirectorySelectorTest/a/file").createNewFile()
    new File("target/DirectorySelectorTest/nested/b/dummy").mkdirs()
    new File("target/DirectorySelectorTest/nested/b/file").createNewFile()
    new File("target/DirectorySelectorTest/nested/c/.git/dummy").mkdirs()
    new File("target/DirectorySelectorTest/nested/c/file").createNewFile()

    val selector = new DirectorySelector()
    assert(selector.select(new File("target/DirectorySelectorTest")) == List(
      new File("target/DirectorySelectorTest/a"),
      new File("target/DirectorySelectorTest/nested/c")))
  }
}
