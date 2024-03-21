package io.github.teonistor.observer.hone

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.node.{JsonNodeFactory, NullNode, ObjectNode}

import java.util.{TreeMap => JTreeMap}
import scala.util.Try

class JsonHoner extends Honer {

  private lazy val objectMapper = JsonMapper.builder()
    .nodeFactory(new JsonNodeFactory(){

      // Make it so object keys are written out alphabetically
      // https://cowtowncoder.medium.com/jackson-tips-sorting-json-using-jsonnode-ce4476e37aee
      override def objectNode(): ObjectNode = new ObjectNode(this, new JTreeMap[String,JsonNode](){

        // Make it so nulls are left out entirely
        override def put(key: String, value: JsonNode): JsonNode = value match {
          case _: NullNode => value
          case _ => super.put(key,value)
        }
      })
    })
    .build()

  override def honeIn(content: String): Option[String] =
    Try(objectMapper.readTree(content).toString)
      .toOption
}
