package io.github.teonistor.scalamacros

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

object WithMocks {
  /**
   * Input usage:
   *
   * withMocks("do something", (a: Apple, b: Banana) => {
   *   someAssertions(a)
   *   moreAssertions(b)
   * })
   *
   * is equivalent to:
   *
   * test("do something") {
   *   val a = mock[Apple]
   *   val b = mock[Banana]
   *
   *   someAssertions(a)
   *   moreAssertions(b)
   * }
   */
  def mocksTest(name: String, func: Any): Unit = macro WithMocks.impl

  def impl(c: whitebox.Context)(name: c.Expr[String], func: c.Expr[Any]) = {
    import c.universe.{Quasiquote, ValDef}
    val nameT = name.tree
    val funcT = func.tree

    val params = funcT match {
      case q"(..$params) => {..$_}" => params.toList.map {
        case ValDef(_,_,typ,_) => typ

        case _=> abort(c, s"could not handle parameters of $funcT")
      }
      case _=> abort(c, s"expected a function as second argument but got $funcT")
    }

    val mockDecls = params.map(typ => q"mock[$typ]")
    val output = q"""
      test($nameT) {
        val func = $funcT
        func(..$mockDecls)
      }"""

    println(s"[DEBUG] WithMocks: Turned $nameT, $funcT into $output")
    output
  }

  private def abort(c: whitebox.Context, message: String) =
    c.abort(c.enclosingPosition, s"withMocks $message")
}
