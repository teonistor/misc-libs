package io.github.teonistor.spotifier.auth

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.json.JsonMapper
import org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString
import org.springframework.web.reactive.function.BodyInserters.fromFormData
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec
import org.springframework.web.reactive.function.client.{WebClient, WebClientResponseException}

import java.nio.file.Files.newInputStream
import java.nio.file.Path.{of => path}
import java.nio.file.{Files, Path}
import java.time.LocalDateTime
import java.time.ZoneOffset.UTC
import scala.util.Try

object AuthHelper {

  private val clientID = "cda668df28904272a5e1e802d20fdc86"
  private val clientSecret = "631881530ec2435cb9a4f3f25b5b0af9"

  def obtainToken() =
    obtainToken0(path("spotify-creds/token.json"), "https://accounts.spotify.com/api/token")

  private[auth] def obtainToken0(credsFile: Path, tokenURI: String) = {

    val objectMapper = JsonMapper.builder().findAndAddModules().build()

    try {
      val fileJson = objectMapper.readTree(newInputStream(credsFile))

      val existingActiveToken = Some(fileJson)
        .filter(_.hasNonNull("access_token"))
        .filter(expiryHasNotExpired)
        .map(_.get("access_token").textValue())
        .filter(_ != null)

      existingActiveToken.getOrElse {
        val existingAccessCode = Some(fileJson)
          .filter(_.hasNonNull("code"))
          .map(_.get("code").textValue())
          .filter(_ != null)
          .getOrElse(throw new IllegalStateException(s"Manual hack needed! Please obtain access code by navigating to: https://accounts.spotify.com/authorize?response_type=code&client_id=$clientID&scope=playlist-read-private+playlist-read-collaborative&redirect_uri=http://127.0.0.1:8080/spotify-redir&state=2365785"))

        val accessToken = WebClient.builder().build()
          .post()
          .uri(tokenURI)
          .body(fromFormData("code", existingAccessCode)
            .`with`("redirect_uri", "http://127.0.0.1:8080/spotify-redir")
            .`with`("grant_type", "authorization_code"))
          .header("content-type", "application/x-www-form-urlencoded").asInstanceOf[RequestHeadersSpec[_]]
          .header("Authorization", "Basic " + encodeBase64URLSafeString(s"$clientID:$clientSecret".getBytes)).asInstanceOf[RequestHeadersSpec[_]]
          .retrieve()
          .bodyToMono(classOf[JsonNode])
          .doOnError(e => e match {
            case ex: WebClientResponseException => println(ex.getResponseBodyAsString)
            case _ => println(e)
          })
          .block()

        val newToken = accessToken.get("access_token").textValue()

        Files.writeString(credsFile, objectMapper.createObjectNode()
          .put("access_token", newToken)
          .put("expiry", LocalDateTime.now().plusSeconds(accessToken.get("expires_in").intValue()).toString)
          .toPrettyString)

        newToken
      }

    } catch {
      case e: Exception => throw new IllegalStateException(s"Manual hack needed! Please obtain access code by navigating to: https://accounts.spotify.com/authorize?response_type=code&client_id=$clientID&scope=playlist-read-private+playlist-read-collaborative&redirect_uri=http://127.0.0.1:8080/spotify-redir&state=2365785", e)
    }
  }

  private def expiryHasNotExpired(json: JsonNode) =
    Try(LocalDateTime.parse(json.get("expiry").textValue()))
      .toOption
      .exists(_.isAfter(LocalDateTime.now(UTC)))
}
