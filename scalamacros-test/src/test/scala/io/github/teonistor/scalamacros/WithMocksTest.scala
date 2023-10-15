package io.github.teonistor.scalamacros

import org.scalatest.funsuite.AnyFunSuiteLike

class WithMocksTest extends AnyFunSuiteLike {

  WithMocks.withMocks("foo", (x:TestType) => {
    assert(x.aMethod == "orange")
//    val y=x.aMethod
//    println(y)
  })

  private def mock[T] = new TestType {
    override def aMethod: String = "orange"
  }

  private[WithMocksTest] trait TestType {
    def aMethod: String
  }
}
