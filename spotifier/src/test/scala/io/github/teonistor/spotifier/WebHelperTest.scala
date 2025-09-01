package io.github.teonistor.spotifier

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funsuite.AnyFunSuiteLike
import org.springframework.web.reactive.function.client.WebClient

import scala.jdk.CollectionConverters.IterableHasAsScala

class WebHelperTest extends AnyFunSuiteLike with BeforeAndAfterAll {

  private val server = new WireMockServer(0)

  test("get repeatedly") {
    server.start()

    server.givenThat(get("/some-resource/1").withHeader("Authorization", equalTo("Magic token")).willReturn(okJson(s"""
        {
          "value": 77,
          "next": "http://localhost:${server.port()}/some-resource/2"
        }
        """)))
    server.givenThat(get("/some-resource/2").withHeader("Authorization", equalTo("Magic token")).willReturn(okJson("""
        {
          "value": 89
        }
        """)))

    val result = new WebHelper(WebClient.builder()
      .defaultHeader("Authorization", "Magic token")
      .build())
      .getRepeatedly(s"http://localhost:${server.port()}/some-resource/1")
      .map(json => json.get("value").asInt())
      .collectList()
      .block()
      .asScala

    assert(result.toList == List(77, 89))

    server.verify(1, getRequestedFor(urlEqualTo("/some-resource/1")))
    server.verify(1, getRequestedFor(urlEqualTo("/some-resource/2")))
    server.stop()
  }

}
