package io.github.teonistor.spotifier

import org.springframework.web.reactive.function.client.WebClientResponseException

import java.io.File
import java.nio.file.Files.{readString, writeString}
import java.nio.file.Path
import java.util.concurrent.Callable
import java.util.concurrent.Executors.newFixedThreadPool
import scala.util.Try


object GeneralUtil {

  private val executor = newFixedThreadPool(8, (r: Runnable) => {
    val thread = new Thread(r)
    thread.setDaemon(true)
    thread
  })

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

  private[spotifier] implicit class VectorParallelMap[T](private val self:Vector[T]) extends AnyVal {

    def parallelMap[R](func:T=>R) = self
      .map[Callable[R]](t => () => func(t))
      .map(executor.submit(_))
      .map(_.get())

    def parallelFlatMap[R](func:T=>IterableOnce[R]) = self
      .map[Callable[IterableOnce[R]]](t => () => func(t))
      .map(executor.submit(_))
      .flatMap(_.get())
  }

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
