package io.github.teonistor.gitbang

import org.scalatest.funsuite.AnyFunSuite

import java.io.File

class RunnerTest extends AnyFunSuite {

  test("run") {

    assert(new Runner(new File("/")).run("echo", "some text I want", "to retrieve") == "some text I want to retrieve")
    assert(new Runner(new File("/tmp")).run("pwd") == "/tmp")
  }
}
