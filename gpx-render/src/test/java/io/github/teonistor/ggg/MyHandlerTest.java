package io.github.teonistor.ggg;

import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3BucketEntity;
import static com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3Entity;
import static com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import static com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3ObjectEntity;

class MyHandlerTest {

    @Test
    void handleRequest() {
        final S3EventNotification input = new S3EventNotification(List.of(
                new S3EventNotificationRecord(
                        "irrelevant",
                        "ObjectCreated:Put",
                        "aws:s3",
                        "1970-01-01T00:00:00.000Z",
                        "2.0",
                        null,
                        null,
                        new S3Entity(
                                "irrelevant",
                                new S3BucketEntity("stuff-in", null, "irrelevant"),
                                new S3ObjectEntity("stuff", 100L, "irrelevant", "irrelevant", "irrelevant"),
                                "1.0"),
                        null)));


    }
}