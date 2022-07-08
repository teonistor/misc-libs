package io.github.teonistor.testing

import org.mockito.BDDMockito.`given`

class SuperstrictMockitoTestBaseTest extends SuperstrictMockitoTestBase {

  test("Mockito by itself doesn't complain about unused stubs") {
    val func = mock[String => String]
    given(func("Banana")).willReturn("Split")
  }

  test("Mockito by itself doesn't complain about misstubbing") {
    val func = mock[String => String]
    given(func("Banana")).willReturn("Split")
    assert(func("Apple") == null)
  }

  test("Mockito by itself doesn't complain about extra interactions") {
    val func = mock[String => String]
    assert(func("Apple") == null)
  }

  // TODO They do indeed fail, but how to capture _that_ as a test?

  mockitoTest("Assisted Mockito complains about unused stubs") {
    val func = mock[String => String]
    given(func("Banana")).willReturn("Split")
  }

  mockitoTest("Assisted Mockito complains about misstubbing") {
    val func = mock[String => String]
    given(func("Banana")).willReturn("Split")
    assert(func("Apple") == null)
  }

  mockitoTest("Assisted Mockito complains about extra interactions") {
    val func = mock[String => String]
    assert(func("Apple") == null)
  }
}
