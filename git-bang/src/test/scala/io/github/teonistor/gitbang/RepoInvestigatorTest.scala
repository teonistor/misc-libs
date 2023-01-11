package io.github.teonistor.gitbang

import io.github.teonistor.testing.SuperstrictMockitoTestBase
import org.mockito.BDDMockito.`given`

class RepoInvestigatorTest extends SuperstrictMockitoTestBase {

  mockitoTest("checked out no fetch", classOf[Runner]) (runner => {
    given(runner.run("git", "remote")).willReturn("origin")
    given(runner.run("git", "branch", "-r")).willReturn("  origin/develop\n  origin/feature/1234")
    given(runner.run("git", "branch")).willReturn("* feature/0000\n  feature/1234\n  feature/5678")
    given(runner.run("git", "diff", "--compact-summary", "origin/develop...feature/0000")).willReturn(" 1 file changed, 1 insertion(+)")
    given(runner.run("git", "diff", "--compact-summary", "origin/develop...feature/1234")).willReturn(" 1 file changed, 1 insertion(+)")
    given(runner.run("git", "diff", "--compact-summary", "origin/develop...feature/5678")).willReturn("")

    assert(new RepoInvestigator(runner, false).call() ==
           RepoInvestigation(Some("origin/develop"), Some("feature/0000"), List("feature/5678"), List("feature/1234")))
  })

  mockitoTest("fetch not checked out", classOf[Runner])(runner => {
    given(runner.run("git", "remote")).willReturn("origin")
    given(runner.run("git", "branch", "-r")).willReturn("  origin/develop\n  origin/feature/1234\n  origin/master")
    given(runner.run("git", "branch")).willReturn("  feature/1234\n  feature/5678")
    given(runner.run("git", "fetch")).willReturn("irrelevant")
    given(runner.run("git", "diff", "--compact-summary", "origin/master...feature/1234")).willReturn(" 1 file changed, 1 insertion(+)")
    given(runner.run("git", "diff", "--compact-summary", "origin/master...feature/5678")).willReturn("")

    assert(new RepoInvestigator(runner, true).call() ==
      RepoInvestigation(Some("origin/master"), None, List("feature/5678"), List("feature/1234")))
  })

  mockitoTest("a few remotes", classOf[Runner])(runner => {
    given(runner.run("git", "remote")).willReturn("origin\nupstream")
    given(runner.run("git", "branch", "-r")).willReturn("  origin/devel\n  origin/feature/1234\n  upstream/prod")
    given(runner.run("git", "branch")).willReturn("* feature/apple\n  feature/banana")
    given(runner.run("git", "diff", "--compact-summary", "origin/devel...feature/apple")).willReturn("")
    given(runner.run("git", "diff", "--compact-summary", "origin/devel...feature/banana")).willReturn(" 1 file changed, 1 deletion(-)")

    assert(new RepoInvestigator(runner, false).call() ==
      RepoInvestigation(Some("origin/devel"), None, List("feature/apple"), List("feature/banana")))
  })
}
