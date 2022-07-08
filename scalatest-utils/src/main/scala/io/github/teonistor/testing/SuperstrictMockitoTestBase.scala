package io.github.teonistor.testing

import org.mockito.Mockito.{mock => mocc}
import org.mockito.{ArgumentMatchersSugar, MockitoScalaSession, MockitoSugar}
import org.scalactic.source.Position
import org.scalatest.funsuite.AnyFunSuite
import org.scalatest.{BeforeAndAfterEach, Tag}

class SuperstrictMockitoTestBase extends AnyFunSuite with MockitoSugar with ArgumentMatchersSugar with BeforeAndAfterEach {

  //  private val superstrictMocks = mutable.Set.empty[AnyRef]

  def beforeMockitoTest(): Unit = ()

  def mockitoTest(name: String, tags: Tag*)(body: => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body
    })(pos)

  def mockitoTest[T1 <: AnyRef](name: String, c1: Class[T1], tags: Tag*)(body: T1 => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body(mocc(c1))
    })(pos)

  def mockitoTest[T1 <: AnyRef, T2 <: AnyRef](name: String, c1: Class[T1], c2: Class[T2], tags: Tag*)(body: (T1, T2) => Any)(implicit pos: Position): Unit =
    test(name, tags: _*)(MockitoScalaSession() run {
      beforeMockitoTest()
      body(mocc(c1), mocc(c2))
    })(pos)

//  def mockitoTest[T1 <: AnyRef: ClassTag: WeakTypeTag](name: String, tags: Tag*)(body: T1 => Any)(implicit pos: Position): Unit =
//    test(name, tags: _*)(MockitoScalaSession() run {
//      beforeMockitoTest()
//      body(mock[T1])
//    })(pos)

//  def mockitoTest[T1 <: AnyRef: ClassTag: WeakTypeTag, T2 <: AnyRef: ClassTag: WeakTypeTag](name: String, tags: Tag*)(body: (T1,T2) => Any)(implicit pos: Position): Unit =
//    test(name, tags: _*)(MockitoScalaSession() run {
//      beforeMockitoTest()
//      body(mock[T1], mock[T2])
//    })(pos)

//  def superstrict[T <: AnyRef](obj: T): T = {
//    superstrictMocks.add(obj)
//    obj
//  }
//
//  override def afterEach(): Unit = {
//    verifyNoMoreInteractions(superstrictMocks.toSeq:_*)
//  }
}
