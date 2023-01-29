package io.github.teonistor.gitbang

import io.github.teonistor.testing.SuperstrictMockitoTestBase
import org.mockito.BDDMockito.`given`

import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit.SECONDS

class RootHandlerTest extends SuperstrictMockitoTestBase {

  mockitoTest("two directories with commands, check parallelism, but don't run them",
    classOf[DirectorySelector], classOf[File => Runner], classOf[(Runner,Boolean) => RepoInvestigator], classOf[CommandMaker], classOf[SituationReportMaker], classOf[IOHelper],
    classOf[Runner], classOf[Runner], classOf[RepoInvestigator], classOf[RepoInvestigator],
    classOf[File], classOf[File], classOf[File],
    classOf[RepoInvestigation], classOf[RepoInvestigation])(
      (directorySelector, runnerFactory, investigatorFactory, commandMaker, situationReportMaker, ioHelper,
       runnerA, runnerB, investigatorA, investigatorB,
       root, dirA, dirB,
       investigationA, investigationB) => {

      val latch = new CountDownLatch(1)

      given(directorySelector.select(root)) willReturn List(dirA, dirB)
      given(runnerFactory(dirA)) willReturn runnerA
      given(runnerFactory(dirB)) willReturn runnerB
      given(investigatorFactory(runnerA, false)) willReturn investigatorA
      given(investigatorFactory(runnerB, false)) willReturn investigatorB
      given(investigatorA.call()) will (_=> {assert(latch.await(2, SECONDS)); investigationA})
      given(investigatorB.call()) will (_=> {latch.countDown(); investigationB})
      given(situationReportMaker.makeMultiple(List(investigationA, investigationB), List(dirA, dirB))).willReturn("Something to be printed")
      given(commandMaker(investigationA)) willReturn List(List("command1", "arg11"), List("command2", "arg21", "arg22"))
      given(commandMaker(investigationB)) willReturn List(List("command1", "arg11", "arg12"), List("command2", "arg21"))
      given(dirA.getAbsolutePath) willReturn "/absolute/path/dirA"
      given(dirB.getAbsolutePath) willReturn "/absolute/path/dirB"
      given(ioHelper.ask("Autorun all? (y/n)")).willReturn("n")

      new RootHandler(directorySelector, runnerFactory, investigatorFactory, commandMaker, situationReportMaker, ioHelper).run(root)
    })

  mockitoTest("one of two directories with commands, and run them",
    classOf[DirectorySelector], classOf[File => Runner], classOf[(Runner, Boolean) => RepoInvestigator], classOf[CommandMaker], classOf[SituationReportMaker], classOf[IOHelper],
    classOf[Runner], classOf[Runner], classOf[RepoInvestigator], classOf[RepoInvestigator],
    classOf[File], classOf[File], classOf[File],
    classOf[RepoInvestigation], classOf[RepoInvestigation])(
    ( directorySelector, runnerFactory, investigatorFactory, commandMaker, situationReportMaker, ioHelper,
      runnerA, runnerB, investigatorA, investigatorB,
      root, dirA, dirB,
      investigationA, investigationB) => {

      given(directorySelector.select(root)) willReturn List(dirA, dirB)
      given(runnerFactory(dirA)) willReturn runnerA
      given(runnerFactory(dirB)) willReturn runnerB
      given(investigatorFactory(runnerA, false)) willReturn investigatorA
      given(investigatorFactory(runnerB, false)) willReturn investigatorB
      given(investigatorA.call()) willReturn investigationA
      given(investigatorB.call()) willReturn investigationB
      given(situationReportMaker.makeMultiple(List(investigationA, investigationB), List(dirA, dirB))).willReturn("Something to be printed")
      given(commandMaker(investigationA)) willReturn List(List("command1", "arg11", "arg12"), List("command2", "arg21", "arg22"))
      given(commandMaker(investigationB)) willReturn List.empty
      given(dirA.getAbsolutePath) willReturn "/absolute/path/dirA"
      given(ioHelper.ask("Autorun all? (y/n)")) willReturn "y"
      given(runnerA.run("command1", "arg11", "arg12")) willReturn "Irrelevant1"
      given(runnerA.run("command2", "arg21", "arg22")) willReturn "Irrelevant2"

      new RootHandler(directorySelector, runnerFactory, investigatorFactory, commandMaker, situationReportMaker, ioHelper).run(root)
    })
}
