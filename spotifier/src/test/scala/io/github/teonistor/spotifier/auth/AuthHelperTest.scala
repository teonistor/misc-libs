package io.github.teonistor.spotifier.auth

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import org.scalatest.funspec.AnyFunSpecLike

import java.nio.file.Files.writeString
import java.nio.file.Path.{of => path}

class AuthHelperTest extends AnyFunSpecLike {

  private val tempTokenFile = path("target/tmp/token.json")

  it("Return existing token") {
    writeString(tempTokenFile, """{"access_token":"definitely a token","expiry":"2099-12-31T22:33:44"}""")

    assert(AuthHelper.obtainToken0(tempTokenFile, "irrelevant") == "definitely a token")
  }

  describe("Manual intervention needed") {

    it("token file nonexistent") {
      tempTokenFile.toFile.delete

      val exception = intercept[IllegalStateException](AuthHelper.obtainToken0(tempTokenFile, "irrelevant"))
      assert(exception.isInstanceOf[IllegalStateException])
      assert(exception.getMessage startsWith "Manual hack needed! Please obtain access code by navigating to:")
    }

    it("token file not JSON") {
      theTest( "inec23jrnp2nc")
    }

    it("token file does not contain an expiry") {
      theTest( """{"access_token":"irrelevant"}""")
    }

    it("token file contains an invalid expiry") {
      theTest( """{"access_token":"irrelevant","expiry":"banana"}""")
    }

    it("token file contains an invalid token") {
      theTest( """{"access_token":{},"expiry":"2099-12-31T22:33:44"}""")
    }

    it("token file contains an expired expiry") {
      theTest( """{"access_token":"irrelevant","expiry":"2025-01-01T22:33:44"}""")
    }

    it("token file contains an unexpired expiry, but no token") {
      theTest( """{"expiry":"2099-12-31T22:33:44"}""")
    }

    def theTest(tokenFileContent: String): Unit = {
      tempTokenFile.toFile.getParentFile.mkdirs()
      writeString(tempTokenFile, tokenFileContent)

      val exception = intercept[IllegalStateException](AuthHelper.obtainToken0(tempTokenFile, "irrelevant"))
      assert(exception.isInstanceOf[IllegalStateException])
      assert(exception.getMessage startsWith "Manual hack needed! Please obtain access code by navigating to:")
    }
  }

  it("Exchange code for token") {

    val server = new WireMockServer(0)
    server.start()
    writeString(tempTokenFile, """{"code":"the_code"}""")

    server.givenThat(post("/token")
      .withRequestBody(equalTo("code=the_code&redirect_uri=http%3A%2F%2F127.0.0.1%3A8080%2Fspotify-redir&grant_type=authorization_code"))
      .withHeader("content-type", matching("application/x-www-form-urlencoded.+"))
      .withHeader("Authorization", matching("Basic .+"))
      .willReturn(okJson("""
            {
              "access_token": "new token",
              "expires_in": 1200
            }""")))

    assert(AuthHelper.obtainToken0(tempTokenFile, s"http://localhost:${server.port}/token") == "new token")

    server.verify(1, postRequestedFor(urlEqualTo("/token")))
    server.stop()
  }
}
