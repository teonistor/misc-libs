package io.github.teonistor.diary.macroe

import scala.annotation.{StaticAnnotation, compileTimeOnly}
import scala.language.experimental.macros
import scala.reflect.macros.whitebox
import scala.reflect.runtime.universe.reify

@compileTimeOnly("enable macro paradise to expand macro annotations")
class PrivNoArg extends StaticAnnotation {
  def macroTransform(annottees: Any*): Seq[Any] = macro PrivNoArg.impl
}

object PrivNoArg {
  def impl(c: whitebox.Context)(annottees: c.Expr[Any]*) = {
    import c.universe.{ClassDef, Quasiquote, Template, Tree, ValDef}

    val inputs = annottees.map(_.tree).toList
    val stuff: List[c.universe.Tree] = inputs match {
      case (classDef: ClassDef) :: more =>

//        println(s"!!! Input: $mods $name $params $template")
        println(s"!!! Input: $classDef")

//        val template1: Template = template.asInstanceOf[Template]
        val body = classDef.impl.body

        val nulls = classDef match {
          case q"..$_ class $_(...$params) extends ..$_ { ..$_ }" =>
//            val ValDef(_,_,t,_) :: Nil = params(1)
//            println(t + "  " + t.getClass)
//            println(params(1).asInstanceOf[List[_]](0).getClass)
            params.toList.asInstanceOf[List[List[Tree]]]
          .map(_.map {
            case ValDef(_, _, typ, _) => typ.toString()
          }.map {
            case "Boolean" => q"false"
            case "Char" => reify('0') q"'\u0000'"
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

        // List(<caseaccessor> <paramaccessor> val comments: Int = _)

        val output = ClassDef(classDef.mods, classDef.name, classDef.tparams, Template(classDef.impl.parents, classDef.impl.self, body.appended(
          q"private def this() = this(...$nulls)"
        )))
        println("!!!! Modified to " + output)

        output :: more

//        classDecl match {
//        case q"..$mods class $className(..$fields) extends ..$parents { ..$body }" =>
//
//
//
//
//          println("!!!! Input " + classDecl)
//          val value =
//            q"""{} ..$mods class $className(..$fields) extends ..$parents {
//                ..$body
//                private def this() = this(null)
//             }"""
//          println("!!!! Modified to " + value)
//
//          value :: more
//
//        case _ => c.abort(c.enclosingPosition, s"Could not handle $classDecl")
//      }

      case somethingElse => somethingElse // Leave it alone
    }
//
//
//    // Amended from accredited boilerplate https://docs.scala-lang.org/overviews/macros/annotations.html
//    val inputs = annottees.map(_.tree).toList
//    val expandees = inputs match {
//      case (param: ValDef) :: (rest@_ :: _) => (param, rest)
//      case (param: TypeDef) :: (rest@_ :: _) => (param, rest)
//      case _ => (EmptyTree, inputs)
//    }
//    println((annottee, expandees))
//    val outputs = expandees
//    c.Expr[Any](Block(outputs, Literal(Constant(()))))
//
//    val q"{..$things}" = annottees.map(_.tree)
//    val stuff : Seq[Tree]= things.map {
//      case classDecl: ClassDef => classDecl match {
//        case q"..$mods class $className(..$fields) extends ..$parents { ..$body }" =>
//          println("!!!! " + classDecl)
//          val value =
//            q"""..$mods class $className(..$fields) extends ..$parents {
//                ..$body
//                private def this() = this(null)
//             }"""
//          value
//        case _ => c.abort(c.enclosingPosition, s"Could not handle $classDecl")
//      }
//      case somethingElse => somethingElse .asInstanceOf[Tree]// Leave it alone
//    }

//    val stuff: Seq[c.universe.Tree] = annottees.map(_.tree).map {
//      case (classDecl: ClassDef) :: Nil => classDecl match {
//        case q"..$mods class $className(..$fields) extends ..$parents { ..$body }" =>
//          println("!!!! " + classDecl)
//          val value =
//            q"""..$mods class $className(..$fields) extends ..$parents {
//                ..$body
//                private def this() = this(null)
//             }"""
//          List(value)
//        case _ => c.abort(c.enclosingPosition, s"Could not handle $classDecl")
//      }
//      case somethingElse => somethingElse // Leave it alone
//    }

    q"{..$stuff}"
  }
}
