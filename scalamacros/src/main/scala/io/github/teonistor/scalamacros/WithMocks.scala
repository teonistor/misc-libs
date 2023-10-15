package io.github.teonistor.scalamacros

import scala.language.experimental.macros
import scala.reflect.macros.whitebox

/**
 * Input usage:
 *
 * withMocks("do something", (a: Apple, b: Banana) => {
 *   someAssertions(a)
 *   moreAssertions(b)
 * })
 *
 * would result in:
 *
 * test("do something") {
 *   val a = mock[Apple]
 *   val b = mock[Banana]
 *
 *   someAssertions(a)
 *   moreAssertions(b)
 * }
 */

object WithMocks {
  def withMocks(name: String, func: Any): Unit = macro WithMocks.impl

  def impl(c: whitebox.Context)(name: c.Expr[String], func: c.Expr[Any]) = {
    import c.universe.{Quasiquote, ValDef}
    val input = s"$name, $func"

    val params = func.tree match {
      case q"(..$params) => {..$body}" => params.toList.map {
        case ValDef(_,name,typ,_) => (name, typ)

        case _ => abort(c, s"could not handle parameters of $input")
      }
      case _ => abort(c, s"expected a function as second argument but got $input")
    }

    val mockDecls = params.map {
      case (name,typ) =>
        println(s"DEBUG::  $name  $typ")
//        q"val $name:$typ = mock[$typ];"
        q"mock[$typ]"
//        ValDef.apply(Modifiers(), name, typ, q"mock[$typ]")
    }
    val output = q"""
      test(${name.tree}) {
        val func = ${func.tree}
        func(..$mockDecls)
      }"""

    println(s"[DEBUG] WithMocks: Turned ${name.tree}, $input into $output")
    output
  }

  private def abort(c: whitebox.Context, message: String) =
    c.abort(c.enclosingPosition, s"withMocks $message")
}
