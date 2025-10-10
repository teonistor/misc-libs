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
  // Meta-TODO: This is fixed in a 4.* version of Mockito - ensure that is given in the usual suspects then this isn't needed
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

# Github Actions Cheatsheet

1. To just build and run tests, start with a basic sample (link TODO)
2. If your Maven-like project has dependencies released in Github Packages and not Maven central, then:
   - You need to grant the pipeline read access to such package repositories, even if the code repositories are public:
     1. Create a token with read access and assign it to your pipeline (link TODO) e.g. as env var PACKAGE_READ_TOKEN
     2. Each Github repository acts as a distinct package repository, so you need to define a repository in your pom for each such dependency e.g.:
       ```xml
       <repositories>
         <repository>
             <id>github-misc-libs</id>
             <url>https://maven.pkg.github.com/teonistor/misc-libs</url>
         </repository>
         ...
       ```
     3. You also need the repository you're working on, of course, e.g.
       ```xml
       <distributionManagement>
         <repository>
             <id>github</id>
             <url>https://maven.pkg.github.com/teonistor/my-lib</url>
         </repository>
       </distributionManagement>
       ```
     4. Create a file `settings.xml` because the one provided by Github is rubbish in this case. We need to give **write** access to the one repository we're working on (by using `GITHUB_TOKEN`, which is provided automatically), and **read** access to the others (by using `PACKAGE_READ_TOKEN`, which we created above) e.g.:
       ```xml
       <settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
          <servers>
            <server>
                <id>github</id>
                <username>teonistor</username>
                <password>${GITHUB_TOKEN}</password>
            </server>
            <server>
                <id>github-heroku-site-lib</id>
                <username>teonistor</username>
                <password>${PACKAGE_READ_TOKEN}</password>
            </server>
            ...
       ```
     5. We then make use of it by defining the commands like this in Github Actions:
        ```yaml
          run: mvn --settings .github/settings.xml -B verify release:prepare release:perform
        ```
3. If your Maven-like project wants to release and is itself a private repo:
   - Due to what I can only assume is a bug, maven-release-plugin cannot run the `git clone` command near the end, so we add this:
     ```xml
       <configuration>
         <localCheckout>true</localCheckout>
         ...
     ```
