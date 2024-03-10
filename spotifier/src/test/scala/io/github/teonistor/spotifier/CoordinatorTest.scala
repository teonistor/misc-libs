package io.github.teonistor.spotifier

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.{get, okJson}
import org.reactivestreams.Publisher
import org.scalatest.funsuite.AnyFunSuite
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import org.springframework.web.reactive.function.client.WebClientResponseException.NotFound
import org.springframework.web.util.UriBuilder
import reactor.core.publisher.{Flux, Mono}
import reactor.core.scheduler.Schedulers

import java.time.LocalDateTime
import java.util.function.{BiFunction, Supplier}
import scala.jdk.CollectionConverters.IterableHasAsJava

class CoordinatorTest extends AnyFunSuite {

  test("Flux experiments") {
    val server = new WireMockServer(0)
    server.start()
    server.setGlobalFixedDelay(5000)
    val url = "http://localhost:" + server.port()

    server.stubFor(get("/1").willReturn(okJson("1")))
    server.stubFor(get("/2").willReturn(okJson("2")))
//    server.stubFor(get("/3").willReturn(notFound()))
    server.stubFor(get("/3").willReturn(okJson("3")))
    server.stubFor(get("/4").willReturn(okJson("4")))

    val web = WebClient.builder()
      .baseUrl(url)
      .build()

    val function: BiFunction[String,String,String] = (value: String, value1: String) => value + value1
    val v= Flux.fromIterable(List(1, 2, 3, 4).asJava)
      .flatMapSequential[Int](i => web
          .get()
          .uri((b: UriBuilder) => b.path(i.toString).build()).asInstanceOf[RequestHeadersSpec[_]]
          .retrieve()
          .bodyToMono(classOf[Int]))
//      .onErrorContinue((_,_) => {})
//      .onErrorComplete(classOf[NotFound])
      .onErrorResume(classOf[NotFound], new java.util.function.Function[NotFound, Publisher[Int]] {
        override def apply(t: NotFound): Publisher[Int] = Mono.empty()
      })
//      .reduce(0, (_: Int) + (_: Int))
      .map(_.toString)
      .reduce[String]("", function)

    println(">>>>>  " + v.block() + "  " + LocalDateTime.now().withNano(0))
    println(">>>>>  " + v.block() + "  " + LocalDateTime.now().withNano(0))
    println(">>>>>  " + v.block() + "  " + LocalDateTime.now().withNano(0))
    println(">>>>>  " + v.block() + "  " + LocalDateTime.now().withNano(0))
//    assert(v.block() == 7)
    /*
    * Conclusion:
    * Even with flatMapSequential, handling errors on the combined flux is erratic when errors do occur (both with onErrorComplete
    * and onErrorResume). onErrorContinue on the combined flux does work, as it propagates back up. but we probably want to handle
    * the error on the original, inner one
    * */
  }

  test("Flux with 'heavy' operations") {
    def difficult(a:Int)= {
      Thread.sleep(5000)
      a
    }

    val function: BiFunction[String,String,String] = (value: String, value1: String) => value + value1
    val v= Flux.fromIterable(List(1, 2, 3, 4).asJava)
      .parallel()
      .runOn(Schedulers.parallel())
      .flatMap[Int](i => {
        val i1: Supplier[Int] = () => difficult(i.asInstanceOf[Int])
        Mono.fromSupplier(i1)
      })
      //      .onErrorContinue((_,_) => {})
      //      .onErrorComplete(classOf[NotFound])
//      .onErrorResume(classOf[NotFound], new java.util.function.Function[NotFound, Publisher[Int]] {
//        override def apply(t: NotFound): Publisher[Int] = Mono.empty()
//      })
      //      .reduce(0, (_: Int) + (_: Int))
      .map[String](_.toString)
      .reduce(function)

    println(">>>>>  " + v.block() + "  " + LocalDateTime.now().withNano(0))
    println(">>>>>  " + v.block() + "  " + LocalDateTime.now().withNano(0))
    println(">>>>>  " + v.block() + "  " + LocalDateTime.now().withNano(0))
    println(">>>>>  " + v.block() + "  " + LocalDateTime.now().withNano(0))
    // Conclusion: It parallelises with great tinkering (and once it did, the range of available operations shrinks)
    // Concurrency vs parallelism: https://stackoverflow.com/questions/69293859/parallel-flux-vs-flux-in-project-reactor#comment122495083_69298350
  }
}
