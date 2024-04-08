package io.github.teonistor.observer.data

import io.github.teonistor.observer.hone.{Honer, JsonHoner, RegexHoner}

import java.util.Objects.{requireNonNull => nn}

case class JobSettings(name: Option[String],
                       url: String,
                       honerStyle: String,
                       honerArgs: Option[List[String]],
                       key: Option[String],
                       topicArn: Option[String]) {

  nn(url,"url must not be null")
  nn(honerStyle,"honerStyle must not be null")
  if (name.isEmpty && (key.isEmpty || topicArn.isEmpty))
    throw new IllegalArgumentException("If you don't specify a name, you must specify key and topicArn")

  lazy val resolveHoner: Honer = honerStyle match {
    case "regex" => new RegexHoner(honerArgs.get.head)
    case "json" => new JsonHoner()
  }

  lazy val resolveKey: String =
    key.getOrElse(s"lastseen/${name.get}")

  def resolveTopicArn(prefix: String): String =
    topicArn.getOrElse(s"$prefix:${name.get}-site-observer")
}
