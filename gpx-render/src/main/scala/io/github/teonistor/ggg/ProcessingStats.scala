package io.github.teonistor.ggg

case class ProcessingStats(processed: Int, pulledIn: Int, ignored: Int, erred: Int) {

  def plus(that: ProcessingStats) = ProcessingStats(
    this.processed + that.processed,
    this.pulledIn + that.pulledIn,
    this.ignored + that.ignored,
    this.erred + that.erred)
}
