package io.github.teonistor.observer.hone

import org.scalatest.funsuite.AnyFunSuite

class JsonHonerTest extends AnyFunSuite {

  test("can hone in") {
    val input =
      """{
        | "b":   7,
        |"c" :null,
        |   "a":[3,   "5",9]
        |}""".stripMargin
    val expectedOutput = """{"a":[3,"5",9],"b":7}"""

    assert(new JsonHoner().honeIn(input).contains(expectedOutput))
  }

  test("cannot hone in") {
    assert(new JsonHoner().honeIn("`gNu37u").isEmpty)
  }
}
