package io.github.teonistor.gitbang

import java.io.File

class SituationReportMaker extends ((RepoInvestigation, File) => String) {

  def apply(investigation: RepoInvestigation, dir: File): String =
    LazyList(
        dir.toString,
        "Remote production branch: " + investigation.remoteProductionBranch.getOrElse("None"),
        investigation.checkedoutBranch.map("Checked out on " +_).getOrElse("Not checked out"),
        if (investigation.toDelete.nonEmpty)
          investigation.toDelete.mkString("The following local branches will be deleted:\n  ", "\n  ", "")
        else "" ,
        if (investigation.toKeep.nonEmpty)
          investigation.toKeep.mkString("The following local branches will be kept:\n  ", "\n  ", "")
        else "" )
      .mkString("\n")

  def makeMultiple(investigations: Seq[RepoInvestigation], dirs: Seq[File]): String =
    (investigations zip dirs)
      .map(tupled)
      .mkString("------- Git Situation Report -------\n",
              "\n------------------------------------\n", "")
}
