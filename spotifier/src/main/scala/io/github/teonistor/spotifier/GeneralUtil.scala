package io.github.teonistor.spotifier

import org.springframework.web.reactive.function.client.WebClientResponseException

import java.io.File
import java.nio.file.Files.{readString, writeString}
import java.nio.file.Path
import scala.util.Try


object GeneralUtil {

  private[spotifier] def cachingObj[T](serialiser: T => String, deserialiser: String => T)(cacheFile: String)(func: => T) =
    deserialiser(cachingString(cacheFile)(serialiser(func)))

  private[spotifier] def cachingString(cacheFile: String)(func: => String) =
    Try(readString(Path.of(cacheFile)))
      .recover { e =>
        println(s"Note: Could not load cache from $cacheFile because $e")
        val data = func

        Try {
          new File(cacheFile).getParentFile.mkdirs()
          writeString(Path.of(cacheFile), data)
        }.recover(e => println(s"Note: Could not save cache to $cacheFile because $e"))

        data
      }.get

  private[spotifier] def ns[T](func: => T) =
    try
      func
    catch {
      case _: NullPointerException => null.asInstanceOf[T]
    }

  private[spotifier] def withWebClientExceptionLogging[T](func: => T) =
    try
      func
    catch {
      case e: WebClientResponseException =>
        System.err.println(e.getResponseBodyAsString)
        throw e
    }
}
