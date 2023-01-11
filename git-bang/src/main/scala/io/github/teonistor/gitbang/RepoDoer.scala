package io.github.teonistor.gitbang

import java.util.concurrent.Callable

class RepoDoer (
      runner: Runner,
      investigation: RepoInvestigation) extends Callable[Boolean] {

  override def call(): Boolean = {
    investigation.remoteProductionBranch
      .filter(_=> investigation.checkedoutBranch.isEmpty)
      .foreach(runner.run("git", "checkout", _))

    investigation.toDelete.foreach(runner.run("git", "branch", "-D", _))

    investigation.remoteProductionBranch
      .filter(_ => investigation.checkedoutBranch.isDefined)
      .foreach(runner.run("git", "merge", _))

    true
  }
}
