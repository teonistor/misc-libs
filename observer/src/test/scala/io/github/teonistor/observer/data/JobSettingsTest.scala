package io.github.teonistor.observer.data

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.json.JsonMapper
import io.github.teonistor.observer.hone.{JsonHoner, RegexHoner}
import org.scalatest.funsuite.AnyFunSuite
import org.springframework.test.util.ReflectionTestUtils.getField

class JobSettingsTest extends AnyFunSuite {

  private val objectMapper = JsonMapper.builder().findAndAddModules().build()

  test("Can deserialise with name and resolve") {
    val input =
      """{
        |  "name": "my-thingy",
        |  "url": "https://nowhere.com/page",
        |  "honerStyle": "regex",
        |  "honerArgs": ["a.+b"]
        |}""".stripMargin

    val jobSettings = objectMapper.readValue(input, classOf[JobSettings])
    assert(jobSettings.name.contains("my-thingy"))
    assert(jobSettings.url == "https://nowhere.com/page")
    assert(jobSettings.resolveHoner.isInstanceOf[RegexHoner])
    assert(getField(jobSettings.resolveHoner, "extractor") == "a.+b")
    assert(jobSettings.resolveKey == "lastseen/my-thingy")
    assert(jobSettings.resolveTopicArn("arn:aws:sns:000:00000") == "arn:aws:sns:000:00000:my-thingy-site-observer")
  }

  test("Can deserialise with key and topicArn and resolve") {
    val input =
      """{
        |  "url": "https://nowhere.com/page",
        |  "honerStyle": "json",
        |  "key": "specific/place",
        |  "topicArn": "specific::topic::arn"
        |}""".stripMargin

    val jobSettings = objectMapper.readValue(input, classOf[JobSettings])
    assert(jobSettings.url == "https://nowhere.com/page")
    assert(jobSettings.resolveHoner.isInstanceOf[JsonHoner])
    assert(jobSettings.resolveKey == "specific/place")
    assert(jobSettings.resolveTopicArn("ignored") == "specific::topic::arn")
  }

  test("Cannot deserialise without url") {
    val exception = intercept[JsonProcessingException] {
      objectMapper.readValue("""{"honerStyle":"nil"}""", classOf[JobSettings])
    }

    assert(exception.getCause.isInstanceOf[NullPointerException])
    assert(exception.getCause.getMessage == "url must not be null")
  }

  test("Cannot deserialise without honerStyle") {
    val exception = intercept[JsonProcessingException] {
      objectMapper.readValue("""{"url":"https://nowhere.com/"}""", classOf[JobSettings])
    }

    assert(exception.getCause.isInstanceOf[NullPointerException])
    assert(exception.getCause.getMessage == "honerStyle must not be null")
  }

  test("Cannot deserialise without name or key and topicArn") {
    val exception = intercept[JsonProcessingException] {
      objectMapper.readValue("""{"url":"https://nowhere.com/","honerStyle":"nil"}""", classOf[JobSettings])
    }

    assert(exception.getCause.isInstanceOf[IllegalArgumentException])
    assert(exception.getCause.getMessage == "If you don't specify a name, you must specify key and topicArn")
  }
}
