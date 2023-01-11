package io.github.teonistor.gitbang

import io.github.teonistor.testing.SuperstrictMockitoTestBase
import org.mockito.BDDMockito.`given`

class RepoDoerTest extends SuperstrictMockitoTestBase {

  mockitoTest("with checkedout branch", classOf[Runner])(runner => {
    val input = RepoInvestigation(
      Some("upstream/prod"),
      Some("feature/0123"),
      List("a", "b"),
      List.empty)
    given(runner.run("git", "branch", "-D", "a")).willReturn("irrelevant")
    given(runner.run("git", "branch", "-D", "b")).willReturn("irrelevant")
    given(runner.run("git", "merge", "upstream/prod")).willReturn("irrelevant")

    assert(new RepoDoer(runner, input).call())
  })

  mockitoTest("without checkedout branch", classOf[Runner])(runner => {
    val input = RepoInvestigation(
      Some("upstream/prod"),
      None,
      List("a", "b"),
      List("c", "d"))
    given(runner.run("git", "checkout", "upstream/prod")).willReturn("irrelevant")
    given(runner.run("git", "branch", "-D", "a")).willReturn("irrelevant")
    given(runner.run("git", "branch", "-D", "b")).willReturn("irrelevant")

    assert(new RepoDoer(runner, input).call())
  })
}
