package io.github.teonistor.gitbang

import io.github.teonistor.testing.SuperstrictMockitoTestBase

import java.io.File

class CommandMakerTest extends SuperstrictMockitoTestBase {

  mockitoTest("with checkedout branch", classOf[Runner])(runner => {
    val input = RepoInvestigation(
      Some("upstream/prod"),
      Some("feature/0123"),
      List("a", "b"),
      List.empty)

    assert(new CommandMaker(new File("/home/me/place"))(input) == LazyList(
      List("git", "branch", "-D", "a"),
      List("git", "branch", "-D", "b"),
      List("git", "merge", "upstream/prod")))
  })

  mockitoTest("without checkedout branch", classOf[Runner])(runner => {
    val input = RepoInvestigation(
      Some("upstream/prod"),
      None,
      List("a", "b"),
      List("c", "d"))

    assert(new CommandMaker(new File("/home/me/place"))(input) == LazyList(
      List("git", "checkout", "upstream/prod"),
      List("git", "branch", "-D", "a"),
      List("git", "branch", "-D", "b")))
  })
}
