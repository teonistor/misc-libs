package io.github.teonistor.observer

import io.github.teonistor.observer.hone.Honer
import org.slf4j.LoggerFactory.getLogger
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono.just
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.{GetObjectRequest, PutObjectRequest}
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest

import java.nio.charset.StandardCharsets.UTF_8

class Observer {
  private lazy val log = getLogger(getClass)

  def handle(webClient: WebClient,
             honer: Honer,
             s3: S3Client,
             bucket: String,
             key: String,
             sns: SnsClient,
             topicArn: String): Unit = {

    def publishNotification(subject: String, additionalMessage: String): Unit =
      sns.publish(PublishRequest.builder()
        .topicArn(topicArn)
        .subject(subject)
        .message(s"$subject:$additionalMessage").build())

    webClient.get()
      .retrieve()
      .bodyToMono(classOf[String])
      .map[Either[String,String]](Right(_))
      .onErrorResume(e => just(Left(e.getMessage)))
      .block()
      .fold(e => publishNotification("Site Observer encountered an error", " could not get page of interest: " + e), body => {
        honer.honeIn(body).fold(publishNotification(
          "Site Observer encountered an error",
          " the content changed too much for the change to be identified. New body:\n" + body)) { newContent =>

         (try {
          val oldContent = new String(s3.getObject(GetObjectRequest.builder()
              .bucket(bucket)
              .key(key).build())
            .readAllBytes(), UTF_8)

          if (oldContent != newContent) {
            publishNotification("Site Observer detected a change",
              s"""
                 |Old content:
                 |$oldContent
                 |New content:
                 |$newContent""".stripMargin)
            Some(newContent)

          } else
            None

          } catch {
            case e =>
              log.error("Could not get old content from bucket", e)
              publishNotification("Site Observer detected a change",
                s"""
                   |Could not get old content from bucket!
                   |New content:
                   |$newContent""".stripMargin)
              Some(newContent)
          })
            .fold(())(newContent => s3.putObject(PutObjectRequest.builder()
              .bucket(bucket)
              .key(key)
              .build(), RequestBody.fromString(newContent, UTF_8)))
        }
      })
  }
}
