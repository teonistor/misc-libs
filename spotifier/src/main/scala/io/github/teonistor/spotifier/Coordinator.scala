package io.github.teonistor.spotifier

import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.json.JsonMapper
import io.github.teonistor.spotifier.GeneralUtil.cachingObj

import java.util.concurrent.Executors.newFixedThreadPool

object Coordinator {

  private val executor = newFixedThreadPool(8, (r: Runnable) => {
    val t = new Thread(r)
    t.setDaemon(true)
    t
  })

  private val objectMapper = JsonMapper.builder()
    .findAndAddModules()
    .configure(FAIL_ON_UNKNOWN_PROPERTIES, false).build()

  // TODO Come here - beautiful though this may be, it doesn't seem to preserve pass-by-name-ness, but more testing needed
  private val cachingJson = cachingObj[JsonNode](_.toPrettyString, objectMapper.readTree) _

}
