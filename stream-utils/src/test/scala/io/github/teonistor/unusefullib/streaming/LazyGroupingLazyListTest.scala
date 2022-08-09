package io.github.teonistor.unusefullib.streaming

import io.github.teonistor.unusefullib.streaming.LazyGroupingLazyList.LazyListHasLazyGroupBy
import org.scalatest.FunSuite

class LazyGroupingLazyListTest extends FunSuite {

  test("Aaa") {

    LazyList.from(1).lazyGroupBy(_=>"")
  }
}
