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
          .flatMap(kv => {
            val (checkedOut, branches) = kv
            branches.map(branch => {

              val toDel = runner.run("git", "diff", "--compact-summary", productionBranch + "..." + branch)
                .strip().isEmpty
              val key = if (toDel) "toDelete"
                else if (checkedOut) "checkedout"
                else "toKeep"

              (key, branch)
            })
          })
          .groupMap(_._1)(_._2)

//          .map(kv => (kv._1, kv._2.groupBy(branch =>
//            runner.run("git", "diff", "--compact-summary", productionBranch + "..." + branch)
//              .strip().isEmpty)))

//          .map(_.strip())
//          .groupBy(branch => runner.run("git", "diff", "--compact-summary", productionBranch + "..." + branch).strip().isEmpty)

        RepoInvestigation(
          Some(productionBranch),
          localBranchesByDeleteByCheckedOut.getOrElse("checkedout", List.empty).headOption,
          localBranchesByDeleteByCheckedOut.getOrElse("toDelete", List.empty),
          localBranchesByDeleteByCheckedOut.getOrElse("toKeep", List.empty))
      })
      .getOrElse(RepoInvestigation(None, None, List.empty, List.empty))
  }
}