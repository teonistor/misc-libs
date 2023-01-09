package io.github.teonistor.gitbang

case class RepoInvestigation(
     remoteProductionBranch: Option[String],
     toDelete: List[String],
     toKeep: List[String])
