package io.github.teonistor.testing

import org.mockito.Mockito.{mock => mocc}
import org.mockito.{ArgumentMatchersSugar, MockitoScalaSession, MockitoSugar}
import org.scalactic.source.Position
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfterEach, Tag}

class SuperstrictMockitoTestBase extends AnyFunSuite with MockitoSugar with ArgumentMatchersSugar with BeforeAndAfterEach {

  /** Like beforeEach(), but ran inside the Mockito session for any mock-related initialisation
   */
  def beforeMockitoTest(): Unit = ()

  // Zero-mock method slightly different

  def mockitoTest(name: String, tags: Tag*)(body: => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body
      afterMockitoTest()
    })(pos)

  // 1 to X mock methods generated by code at the bottom

  def mockitoTest[T1 <: AnyRef](name: String, c1: Class[T1], tags: Tag*)(body: (T1) => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body(mocc(c1))
      afterMockitoTest()
    })(pos)

  def mockitoTest[T1 <: AnyRef, T2 <: AnyRef](name: String, c1: Class[T1], c2: Class[T2], tags: Tag*)(body: (T1, T2) => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body(mocc(c1), mocc(c2))
      afterMockitoTest()
    })(pos)

  def mockitoTest[T1 <: AnyRef, T2 <: AnyRef, T3 <: AnyRef](name: String, c1: Class[T1], c2: Class[T2], c3: Class[T3], tags: Tag*)(body: (T1, T2, T3) => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body(mocc(c1), mocc(c2), mocc(c3))
      afterMockitoTest()
    })(pos)

  def mockitoTest[T1 <: AnyRef, T2 <: AnyRef, T3 <: AnyRef, T4 <: AnyRef](name: String, c1: Class[T1], c2: Class[T2], c3: Class[T3], c4: Class[T4], tags: Tag*)(body: (T1, T2, T3, T4) => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body(mocc(c1), mocc(c2), mocc(c3), mocc(c4))
      afterMockitoTest()
    })(pos)

  def mockitoTest[T1 <: AnyRef, T2 <: AnyRef, T3 <: AnyRef, T4 <: AnyRef, T5 <: AnyRef](name: String, c1: Class[T1], c2: Class[T2], c3: Class[T3], c4: Class[T4], c5: Class[T5], tags: Tag*)(body: (T1, T2, T3, T4, T5) => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body(mocc(c1), mocc(c2), mocc(c3), mocc(c4), mocc(c5))
      afterMockitoTest()
    })(pos)

  def mockitoTest[T1 <: AnyRef, T2 <: AnyRef, T3 <: AnyRef, T4 <: AnyRef, T5 <: AnyRef, T6 <: AnyRef](name: String, c1: Class[T1], c2: Class[T2], c3: Class[T3], c4: Class[T4], c5: Class[T5], c6: Class[T6], tags: Tag*)(body: (T1, T2, T3, T4, T5, T6) => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body(mocc(c1), mocc(c2), mocc(c3), mocc(c4), mocc(c5), mocc(c6))
      afterMockitoTest()
    })(pos)

  def mockitoTest[T1 <: AnyRef, T2 <: AnyRef, T3 <: AnyRef, T4 <: AnyRef, T5 <: AnyRef, T6 <: AnyRef, T7 <: AnyRef](name: String, c1: Class[T1], c2: Class[T2], c3: Class[T3], c4: Class[T4], c5: Class[T5], c6: Class[T6], c7: Class[T7], tags: Tag*)(body: (T1, T2, T3, T4, T5, T6, T7) => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body(mocc(c1), mocc(c2), mocc(c3), mocc(c4), mocc(c5), mocc(c6), mocc(c7))
      afterMockitoTest()
    })(pos)

  def mockitoTest[T1 <: AnyRef, T2 <: AnyRef, T3 <: AnyRef, T4 <: AnyRef, T5 <: AnyRef, T6 <: AnyRef, T7 <: AnyRef, T8 <: AnyRef](name: String, c1: Class[T1], c2: Class[T2], c3: Class[T3], c4: Class[T4], c5: Class[T5], c6: Class[T6], c7: Class[T7], c8: Class[T8], tags: Tag*)(body: (T1, T2, T3, T4, T5, T6, T7, T8) => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body(mocc(c1), mocc(c2), mocc(c3), mocc(c4), mocc(c5), mocc(c6), mocc(c7), mocc(c8))
      afterMockitoTest()
    })(pos)

  def mockitoTest[T1 <: AnyRef, T2 <: AnyRef, T3 <: AnyRef, T4 <: AnyRef, T5 <: AnyRef, T6 <: AnyRef, T7 <: AnyRef, T8 <: AnyRef, T9 <: AnyRef](name: String, c1: Class[T1], c2: Class[T2], c3: Class[T3], c4: Class[T4], c5: Class[T5], c6: Class[T6], c7: Class[T7], c8: Class[T8], c9: Class[T9], tags: Tag*)(body: (T1, T2, T3, T4, T5, T6, T7, T8, T9) => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body(mocc(c1), mocc(c2), mocc(c3), mocc(c4), mocc(c5), mocc(c6), mocc(c7), mocc(c8), mocc(c9))
      afterMockitoTest()
    })(pos)

  def mockitoTest[T1 <: AnyRef, T2 <: AnyRef, T3 <: AnyRef, T4 <: AnyRef, T5 <: AnyRef, T6 <: AnyRef, T7 <: AnyRef, T8 <: AnyRef, T9 <: AnyRef, T10 <: AnyRef](name: String, c1: Class[T1], c2: Class[T2], c3: Class[T3], c4: Class[T4], c5: Class[T5], c6: Class[T6], c7: Class[T7], c8: Class[T8], c9: Class[T9], c10: Class[T10], tags: Tag*)(body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10) => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body(mocc(c1), mocc(c2), mocc(c3), mocc(c4), mocc(c5), mocc(c6), mocc(c7), mocc(c8), mocc(c9), mocc(c10))
      afterMockitoTest()
    })(pos)

  def mockitoTest[T1 <: AnyRef, T2 <: AnyRef, T3 <: AnyRef, T4 <: AnyRef, T5 <: AnyRef, T6 <: AnyRef, T7 <: AnyRef, T8 <: AnyRef, T9 <: AnyRef, T10 <: AnyRef, T11 <: AnyRef](name: String, c1: Class[T1], c2: Class[T2], c3: Class[T3], c4: Class[T4], c5: Class[T5], c6: Class[T6], c7: Class[T7], c8: Class[T8], c9: Class[T9], c10: Class[T10], c11: Class[T11], tags: Tag*)(body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11) => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body(mocc(c1), mocc(c2), mocc(c3), mocc(c4), mocc(c5), mocc(c6), mocc(c7), mocc(c8), mocc(c9), mocc(c10), mocc(c11))
      afterMockitoTest()
    })(pos)

  def mockitoTest[T1 <: AnyRef, T2 <: AnyRef, T3 <: AnyRef, T4 <: AnyRef, T5 <: AnyRef, T6 <: AnyRef, T7 <: AnyRef, T8 <: AnyRef, T9 <: AnyRef, T10 <: AnyRef, T11 <: AnyRef, T12 <: AnyRef](name: String, c1: Class[T1], c2: Class[T2], c3: Class[T3], c4: Class[T4], c5: Class[T5], c6: Class[T6], c7: Class[T7], c8: Class[T8], c9: Class[T9], c10: Class[T10], c11: Class[T11], c12: Class[T12], tags: Tag*)(body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12) => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body(mocc(c1), mocc(c2), mocc(c3), mocc(c4), mocc(c5), mocc(c6), mocc(c7), mocc(c8), mocc(c9), mocc(c10), mocc(c11), mocc(c12))
      afterMockitoTest()
    })(pos)

  def mockitoTest[T1 <: AnyRef, T2 <: AnyRef, T3 <: AnyRef, T4 <: AnyRef, T5 <: AnyRef, T6 <: AnyRef, T7 <: AnyRef, T8 <: AnyRef, T9 <: AnyRef, T10 <: AnyRef, T11 <: AnyRef, T12 <: AnyRef, T13 <: AnyRef](name: String, c1: Class[T1], c2: Class[T2], c3: Class[T3], c4: Class[T4], c5: Class[T5], c6: Class[T6], c7: Class[T7], c8: Class[T8], c9: Class[T9], c10: Class[T10], c11: Class[T11], c12: Class[T12], c13: Class[T13], tags: Tag*)(body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13) => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body(mocc(c1), mocc(c2), mocc(c3), mocc(c4), mocc(c5), mocc(c6), mocc(c7), mocc(c8), mocc(c9), mocc(c10), mocc(c11), mocc(c12), mocc(c13))
      afterMockitoTest()
    })(pos)

  def mockitoTest[T1 <: AnyRef, T2 <: AnyRef, T3 <: AnyRef, T4 <: AnyRef, T5 <: AnyRef, T6 <: AnyRef, T7 <: AnyRef, T8 <: AnyRef, T9 <: AnyRef, T10 <: AnyRef, T11 <: AnyRef, T12 <: AnyRef, T13 <: AnyRef, T14 <: AnyRef](name: String, c1: Class[T1], c2: Class[T2], c3: Class[T3], c4: Class[T4], c5: Class[T5], c6: Class[T6], c7: Class[T7], c8: Class[T8], c9: Class[T9], c10: Class[T10], c11: Class[T11], c12: Class[T12], c13: Class[T13], c14: Class[T14], tags: Tag*)(body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14) => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body(mocc(c1), mocc(c2), mocc(c3), mocc(c4), mocc(c5), mocc(c6), mocc(c7), mocc(c8), mocc(c9), mocc(c10), mocc(c11), mocc(c12), mocc(c13), mocc(c14))
      afterMockitoTest()
    })(pos)

  def mockitoTest[T1 <: AnyRef, T2 <: AnyRef, T3 <: AnyRef, T4 <: AnyRef, T5 <: AnyRef, T6 <: AnyRef, T7 <: AnyRef, T8 <: AnyRef, T9 <: AnyRef, T10 <: AnyRef, T11 <: AnyRef, T12 <: AnyRef, T13 <: AnyRef, T14 <: AnyRef, T15 <: AnyRef](name: String, c1: Class[T1], c2: Class[T2], c3: Class[T3], c4: Class[T4], c5: Class[T5], c6: Class[T6], c7: Class[T7], c8: Class[T8], c9: Class[T9], c10: Class[T10], c11: Class[T11], c12: Class[T12], c13: Class[T13], c14: Class[T14], c15: Class[T15], tags: Tag*)(body: (T1, T2, T3, T4, T5, T6, T7, T8, T9, T10, T11, T12, T13, T14, T15) => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body(mocc(c1), mocc(c2), mocc(c3), mocc(c4), mocc(c5), mocc(c6), mocc(c7), mocc(c8), mocc(c9), mocc(c10), mocc(c11), mocc(c12), mocc(c13), mocc(c14), mocc(c15))
      afterMockitoTest()
    })(pos)

  /** Like afterEach(), but ran inside the Mockito session for any mock-related teardown. Note the mockito session is level-3-strict
   *  (complains even about extra interactions), thus it's unlikely this will ever be needed
   */
  def afterMockitoTest(): Unit = ()

}

// Uncomment and use responsibly
//object SuperstrictMockitoTestBase {
//  val howManyBlocks = 15
//
//  def main(arg: Array[String]): Unit = {
//    println((1 to howManyBlocks)
//      .map(generateCode)
//      .mkString("\n"))
//  }
//
//  private def generateCode(howManyParams: Int): String = {
//    val types = (1 to howManyParams).map(i => s"T$i <: AnyRef").mkString(", ")
//    val classes = (1 to howManyParams).map(i => s"c$i: Class[T$i]").mkString(", ")
//    val args = (1 to howManyParams).map(i => s"T$i").mkString(", ")
//    val mocks = (1 to howManyParams).map(i => s"mocc(c$i)").mkString(", ")
//
//    s"""  def mockitoTest[$types](name: String, $classes, tags: Tag*)(body: ($args) => Any)(implicit pos: Position): Unit =
//       |    test(name, tags: _*)(MockitoScalaSession() run {
//       |      beforeMockitoTest()
//       |      body($mocks)
//       |      afterMockitoTest()
//       |    })(pos)
//       |""".stripMargin
//  }
//}
