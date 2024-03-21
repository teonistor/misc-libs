package io.github.teonistor.gitbang

import org.scalatest.funsuite.AnyFunSuite

class CommandMakerTest extends AnyFunSuite {

  test("with checkedout branch") {
    val input = RepoInvestigation(
      Some("upstream/prod"),
      Some("feature/0123"),
      List("a", "b"),
      List.empty)

    assert(new CommandMaker()(input) == LazyList(
      List("git", "branch", "-D", "a"),
      List("git", "branch", "-D", "b"),
      List("git", "merge", "upstream/prod")))
  }

  test("without checkedout branch") {
    val input = RepoInvestigation(
      Some("upstream/prod"),
      None,
      List("a", "b"),
      List("c", "d"))

    assert(new CommandMaker()(input) == LazyList(
      List("git", "checkout", "upstream/prod"),
      List("git", "branch", "-D", "a"),
      List("git", "branch", "-D", "b")))
  }
}
