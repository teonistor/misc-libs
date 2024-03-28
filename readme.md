# The Usual Dependency Suspects

\[blurb todo]

## Mockito Scala Cheatsheet

### Without extra library

```scala
import org.mockito.{ArgumentMatchersSugar, IdiomaticMockito}
import org.mockito.captor.{ArgCaptor => captor}
import org.mockito.stubbing.{DefaultAnswer, ReturnsDefaults}
import org.scalatest.BeforeAndAfter
import org.scalatest.funsuite.AnyFunSuite

class MyTest extends AnyFunSuite with IdiomaticMockito with ArgumentMatchersSugar with BeforeAndAfter {
  // To get around the LocationFactory bug which the default default answer, ReturnsSmartNulls, stumbles upon
  private implicit val defaultAnswer: DefaultAnswer = ReturnsDefaults

  private val classDependency = mock[MyClassDependency]
  private val difficultParameter = captor[ParameterClass]
  
  // any other once-per-class init
  
  test("some test") {
    withObjectMocked[ObjectDependency.type] {
      classDependency.method(difficultParameter) shouldReturn "something"
      ObjectDependency.method() shouldReturn "something else"
      
      // execution and assertion(s)
      // difficultParameter.value...

      classDependency.method(*) wasCalled once
      ObjectDependency.method() wasCalled once
    }
  }
  
  after {
    classDependency wasNever calledAgain
    reset(classDependency)
  }
}
```

