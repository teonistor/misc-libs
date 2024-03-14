package io.github.teonistor.observer

import com.fasterxml.jackson.databind.json.JsonMapper
import io.github.teonistor.observer.data.JobSettings
import org.springframework.web.reactive.function.client.WebClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.sns.SnsClient

import java.io.{InputStream, OutputStream}
import java.nio.charset.StandardCharsets.UTF_8

class Handler {
  private val objectMapper = JsonMapper.builder().findAndAddModules().build()

  def handle(input: InputStream, output: OutputStream): Unit = {
    doHandle(objectMapper.readValue(input, classOf[JobSettings]))
    output.write("OK".getBytes(UTF_8))
  }

  private def doHandle(jobSettings: JobSettings): Unit =
    new Observer().handle(
      WebClient.builder()
        .codecs(_.defaultCodecs().maxInMemorySize(1024 * 1024 * 1024))
        .baseUrl(jobSettings.url)
        .build(),
      jobSettings.resolveHoner,
      S3Client.create(),
      System.getenv("S3_BUCKET"),
      jobSettings.resolveKey,
      SnsClient.create(),
      jobSettings.resolveTopicArn(System.getenv("SNS_TOPIC_PREFIX")))
}
