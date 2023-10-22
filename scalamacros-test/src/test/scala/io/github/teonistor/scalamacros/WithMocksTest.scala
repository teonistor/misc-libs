package io.github.teonistor.scalamacros

import org.scalatest.funsuite.AnyFunSuiteLike

class WithMocksTest extends AnyFunSuiteLike {

  test("incorrect use") {
    assertTypeError("""
      WithMocks.mocksTest("potato", "not a function")
    """)
  }

  WithMocks.mocksTest("correct use", (x:TestType) =>
    assert(x.aMethod == "orange"))

  private def mock[T] = new TestType {
    override def aMethod: String = "orange"
  }

  private[WithMocksTest] trait TestType {
    def aMethod: String
  }
}
