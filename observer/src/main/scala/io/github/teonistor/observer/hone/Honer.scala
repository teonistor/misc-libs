package io.github.teonistor.observer.hone

trait Honer {
  def honeIn(content:String): Option[String]
}
