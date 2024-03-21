package io.github.teonistor.gitbang

class CommandMaker extends (RepoInvestigation => Seq[Seq[String]]){

  def apply(investigation: RepoInvestigation): Seq[Seq[String]] =
    LazyList.concat(
      investigation.remoteProductionBranch
        .filter(_=> investigation.checkedoutBranch.isEmpty)
        .map(List("git", "checkout", _)),

      investigation.toDelete.map(List("git", "branch", "-D", _)),

      investigation.remoteProductionBranch
        .filter(_ => investigation.checkedoutBranch.isDefined)
        .map(List("git", "merge", _)))
}
