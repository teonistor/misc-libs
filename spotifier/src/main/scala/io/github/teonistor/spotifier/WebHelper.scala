package io.github.teonistor.spotifier

import com.fasterxml.jackson.databind.JsonNode
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import reactor.core.publisher.Mono

class WebHelper (web: WebClient){

  def getRepeatedly(startingURI: String) =
    getJson(startingURI).expand(json =>
      if (json.hasNonNull("next"))
        getJson(json.get("next").textValue())
      else
        Mono.empty())

  private def getJson(uri: String) =
    web.get()
      .uri(uri).asInstanceOf[RequestHeadersSpec[_]]
      .retrieve()
      .bodyToMono(classOf[JsonNode])
}
