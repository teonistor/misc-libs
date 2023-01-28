package io.github.teonistor.gitbang

case class RepoInvestigation(
     remoteProductionBranch: Option[String],
     checkedoutBranch: Option[String],
     toDelete: List[String],
     toKeep: List[String])