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

  // We don't need the real mock() method, just something to call by that name with the right signature
  private def mock[T] = new TestType {
    override def aMethod: String = "orange"
  }

  private[WithMocksTest] trait TestType {
    def aMethod: String
  }
}
