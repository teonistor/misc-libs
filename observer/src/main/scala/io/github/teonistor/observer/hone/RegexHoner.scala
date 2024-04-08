package io.github.teonistor.observer.hone

class RegexHoner(extractor: String) extends Honer {

  override def honeIn(content: String): Option[String] =
    extractor.r
      .findAllMatchIn(content)
      .map(_.group(0))
      .toList match {
        case head :: Nil => Option(head)
        case _ => None
      }
}
