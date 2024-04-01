package io.github.teonistor.ggg

import java.net.URLDecoder
import java.nio.charset.StandardCharsets.UTF_8
import java.util.function.UnaryOperator

class NameDecoder extends UnaryOperator[String] {

  override def apply(t: String): String =
    URLDecoder.decode(t, UTF_8)
}
