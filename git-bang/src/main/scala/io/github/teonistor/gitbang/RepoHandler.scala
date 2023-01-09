package io.github.teonistor.gitbang

import java.util.concurrent.Callable

class RepoHandler (
      runner: Runner,
      fetch: Boolean) extends Callable[RepoInvestigation] {

  private val productionBranchSearchOrder = List("master", "main", "prod", "develop", "devel")

  override def call(): RepoInvestigation = {

    val remotes = runner.run("git", "remote").split("\n")
    val remote = if (remotes contains "origin") "origin" else remotes.head // TODO No remotes

    if (fetch)
      runner.run("git", "fetch")

    val remoteBranches = runner.run("git", "branch", "-r")
      .split("\n").to(Set)
      .map(_.strip())
      .filter(_.startsWith(remote))

    productionBranchSearchOrder
      .map(remote + "/" + _)
      .find(remoteBranches.contains).map(productionBranch => {
        val localBranchesByDelete = runner.run("git", "branch")
          .split("\n").to(List)
          .map(_.strip())
          .groupBy(branch => runner.run("git", "diff", "--compact-summary", productionBranch + "..." + branch).strip().isEmpty)

        RepoInvestigation(Some(productionBranch), localBranchesByDelete.getOrElse(true, List.empty), localBranchesByDelete.getOrElse(false, List.empty))
      })
      .getOrElse(RepoInvestigation(None, List.empty, List.empty))
  }
}