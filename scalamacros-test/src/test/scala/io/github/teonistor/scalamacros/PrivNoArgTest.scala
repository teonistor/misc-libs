package io.github.teonistor.scalamacros

class PrivNoArgTest extends org.scalatest.funsuite.AnyFunSuiteLike {

  test("cannot annotate a method") {
    assertTypeError("""
      @PrivNoArg
      def aFunction() = 42
    """)
  }

  test("annotate a class") {
    TestClass.empty.assertCorrectness()
  }

  @PrivNoArg
  class TestClass(aString: String,
                  anInt: Int,
                  aShort: Short,
                  aDouble: Double,
                  aFloat: Float,
                  aChar: Char,
                  aBoolean: Boolean,
                  aByte: Byte,
                  aLong: Long,
                  aList: List[Int],
                  anOption: Option[String]) {

    def assertCorrectness(): Unit = {
      assert(aString == "")
      assert(anInt == 0)
      assert(aShort == 0)
      assert(aDouble == 0.0)
      assert(aFloat == 0F)
      assert(aChar == '\u0000')
      assert(!aBoolean)
      assert(aByte == 0)
      assert(aLong == 0L)
      assert(anInt == 0)
      assert(aList eq null)
      assert(anOption eq null)
    }
  }

  object TestClass {
    def empty:TestClass = new TestClass()
  }
}
