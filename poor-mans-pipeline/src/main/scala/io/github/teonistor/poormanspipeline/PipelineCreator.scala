package io.github.teonistor.poormanspipeline

import org.rogach.scallop.ScallopConf

class PipelineCreator {

  class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    val apples = opt[Int](required = true)
    val bananas = opt[Int]()
    val name = trailArg[String]()
    verify()
  }

  def bob() = {
    new Conf(Seq("--bananas", "7"))
  }

}
