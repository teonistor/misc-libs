package io.github.teonistor.observer.hone

import org.scalatest.funsuite.AnyFunSuite

class RegexHonerTest extends AnyFunSuite {

  test("can hone in") {
    val expectedOutput =
      """<h3 class="wp-block-heading">upcoming</h3>
        |bla bla vla
        |</article>""".stripMargin
    val input =
      s"""<h3 class="sd-title">Share this:</h3>
         |Garbage content before
         |Garbage content before
         |Garbage content before
         |$expectedOutput
         |Garbage content after
         |Garbage content after
         |khsfo9wru-23r=-3o
         |<> { </article>
         |""".stripMargin

    assert(new RegexHoner("""<h3[^>]*>upcoming</h3>[\s\S]+?</article>""").honeIn(input).contains(expectedOutput))
  }

  test("cannot hone in when multiple found") {
    val input =
      s"""<h3 class="a">upcoming</h3>
         |</article>
         |Garbage content
         |<h3 class="b">upcoming</h3>
         |<> { </article>
         |""".stripMargin

    assert(new RegexHoner("""<h3[^>]*>upcoming</h3>[\s\S]+?</article>""").honeIn(input).isEmpty)
  }

  test("cannot hone in when none found") {
    val input =
      s"""<h3 class="sd-title">Share this:</h3>
         |Garbage content
         |Garbage content
         |Garbage content
         |khsfo9wru-23r=-3o
         |<> { </article>
         |""".stripMargin

    assert(new RegexHoner("""<h3[^>]*>upcoming</h3>[\s\S]+?</article>""").honeIn(input).isEmpty)
  }
}
