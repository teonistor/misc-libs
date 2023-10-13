package io.github.teonistor.scalamacros

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox

@compileTimeOnly("enable macro paradise to expand macro annotations")
class PrivNoArg extends StaticAnnotation {
  def macroTransform(annottees: Any*): Seq[Any] = macro PrivNoArg.impl
}

object PrivNoArg {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*) = {
    import c.universe.{ClassDef, Quasiquote, Template, Tree, ValDef}

    val inputs = annottees.map(_.tree).toList
    val outputs = inputs match {
      case (input: ClassDef) :: more =>

        val defaults = input match {
          case q"..$_ class $_(...$params) extends ..$_ { ..$_ }" => params.toList.asInstanceOf[List[List[Tree]]]
            . map(_.map {
              case ValDef(_, _, typ, _) => typ.toString()
            } map {
              case "Boolean" => q"false"
              case "Char" => q"'\u0000'"
              case "Byte" => q"0"
              case "Short" => q"0"
              case "Int" => q"0"
              case "Long" => q"0L"
              case "Float" => q"0f"
              case "Double" => q"0.0"
              case "String" => q""""""""
              case _ => q"null"
            })
        }

        val output = ClassDef(input.mods, input.name, input.tparams, Template(input.impl.parents, input.impl.self, input.impl.body.appended(
          q"private def this() = this(...$defaults)"
        )))

        println(s"[DEBUG] @PrivNoArg: Turned $input into $output")

        output :: more

      case _ => c.abort(c.enclosingPosition, s"@PrivNoArg is meant for classes but found ${inputs.mkString(", ")}")
    }

    q"{..$outputs}"
  }
}
