package io.github.teonistor.observer

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import io.github.teonistor.observer.hone.Honer
import org.mockito.ArgumentMatchersSugar.{any, eqTo}
import org.mockito.IdiomaticMockito
import org.mockito.stubbing.ReturnsDefaults
import org.scalatest.funsuite.AnyFunSuite
import org.springframework.web.reactive.function.client.WebClient
import software.amazon.awssdk.core.ResponseInputStream
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, GetObjectResponse, PutObjectRequest}
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest

import java.io.ByteArrayInputStream
import java.nio.charset.StandardCharsets.UTF_8

class ObserverTest extends AnyFunSuite with IdiomaticMockito {

  private val server = new WireMockServer(0)
  server.start()

  private val webClient = WebClient.builder()
    .baseUrl("http://localhost:" + server.port())
    .build()
  private val honer = mock[Honer](ReturnsDefaults)
  private val s3 = mock[S3Client](ReturnsDefaults)
  private val sns = mock[SnsClient](ReturnsDefaults)
  private val s3Response = mock[GetObjectResponse](ReturnsDefaults)

  private val observer = new Observer()

  test("cannot get => send error notification") {
    server.stubFor(get("/").willReturn(notFound()))

    observer.handle(webClient, honer, s3, "irrelevant", "irrelevant", sns, "some ARN")

    sns.publish(PublishRequest.builder()
      .topicArn("some ARN")
      .subject("Site Observer encountered an error")
      .message("Site Observer encountered an error: could not get page of interest: 404 Not Found from GET http://localhost:" + server.port())
      .build()) wasCalled once
    s3 wasNever calledAgain
  }

  test("cannot extract => send error notification") {
    server.stubFor(get("/").willReturn(ok("response body")))
    honer.honeIn("response body") returns None

    observer.handle(webClient, honer, s3, "irrelevant", "irrelevant", sns, "some ARN")

    sns.publish(PublishRequest.builder()
      .topicArn("some ARN")
      .subject("Site Observer encountered an error")
      .message("Site Observer encountered an error: the content changed too much for the change to be identified. New body:\nresponse body")
      .build()) wasCalled once
    s3 wasNever calledAgain
  }

  test("extract different => update & send modification notification") {
    server.stubFor(get("/").willReturn(ok("response body")))
    honer.honeIn("response body") returns Some("new stuff")
    s3.getObject(GetObjectRequest.builder()
      .bucket("some bucket")
      .key("some key")
      .build()) returns new ResponseInputStream(s3Response, new ByteArrayInputStream("old stuff".getBytes(UTF_8)))

    observer.handle(webClient, honer, s3, "some bucket", "some key", sns, "some ARN")

    sns.publish(PublishRequest.builder()
      .topicArn("some ARN")
      .subject("Site Observer detected a change")
      .message("""Site Observer detected a change:
                 |Old content:
                 |old stuff
                 |New content:
                 |new stuff""".stripMargin)
      .build()) wasCalled once
    s3.putObject(eqTo(PutObjectRequest.builder()
      .bucket("some bucket")
      .key("some key")
      .build()),
      // TODO The request body is squeezed through some lambdas and testing it is a pain
      any[RequestBody]) wasCalled once
    reset(s3)
  }

  test("extract and cannot get old => update & send notification") {
    server.stubFor(get("/").willReturn(ok("response body")))
    honer.honeIn("response body") returns Some("some stuff")
    s3.getObject(GetObjectRequest.builder()
      .bucket("some bucket")
      .key("some key")
      .build()) shouldThrow SdkClientException.create("test error")

    observer.handle(webClient, honer, s3, "some bucket", "some key", sns, "some ARN")

    sns.publish(PublishRequest.builder()
      .topicArn("some ARN")
      .subject("Site Observer detected a change")
      .message("""Site Observer detected a change:
                 |Could not get old content from bucket!
                 |New content:
                 |some stuff""".stripMargin)
      .build()) wasCalled once
    s3.putObject(eqTo(PutObjectRequest.builder()
      .bucket("some bucket")
      .key("some key")
      .build()),
      // TODO The request body is squeezed through some lambdas and testing it is a pain
      any[RequestBody]) wasCalled once
    reset(s3)
  }

  test("extract same => nothing is sent") {
    server.stubFor(get("/").willReturn(ok("response body")))
    honer.honeIn("response body") returns Some("same stuff")
    s3.getObject(GetObjectRequest.builder()
      .bucket("some bucket")
      .key("some key")
      .build()) returns new ResponseInputStream(s3Response, new ByteArrayInputStream("same stuff".getBytes(UTF_8)))

    observer.handle(webClient, honer, s3, "some bucket", "some key", sns, "irrelevant")

    sns wasNever calledAgain
  }
}
