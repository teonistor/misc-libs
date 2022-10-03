package io.github.teonistor.unusefullib.streaming

object LazyGroupingLazyList {

//  def lazyGroupBy[K](k:A=>K):Map[K,LazyList[A]]
//  def lazyGroupMap[K,V](k:A=>K)(v:A=>V):Map[K,LazyList[V]]

  implicit class LazyListHasLazyGroupBy[A](ll:LazyList[A]) {
    def lazyGroupBy[K](k: A => K): Map[K, LazyList[A]] = {
      ???
    }

    def lazyGroupMap[K, V](k: A => K)(v: A => V): Map[K, LazyList[V]] = {
      ???
    }
  }
}
