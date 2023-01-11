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
      .find(remoteBranches.contains)
      .map(productionBranch => {
        val localBranchesByDeleteByCheckedOut = runner.run("git", "branch")
          .split("\n").to(List)
          .groupMap(_.startsWith("*"))(_.substring(1).strip()).to(List)
          .flatMap(keyify(productionBranch).tupled)
          .groupMap(_._1)(_._2)

        RepoInvestigation(
          Some(productionBranch),
          localBranchesByDeleteByCheckedOut.getOrElse("checkedout", List.empty).headOption,
          localBranchesByDeleteByCheckedOut.getOrElse("toDelete", List.empty),
          localBranchesByDeleteByCheckedOut.getOrElse("toKeep", List.empty))
      })
      .getOrElse(RepoInvestigation(None, None, List.empty, List.empty))
  }

  private def keyify(productionBranch: String) =
    (checkedout: Boolean, branches: List[String]) =>
      branches.map(branch =>

        ((if (runner.run("git", "diff", "--compact-summary", productionBranch + "..." + branch)
          .strip().isEmpty) "toDelete"
        else if (checkedout) "checkedout"
        else "toKeep"),
          branch))
}