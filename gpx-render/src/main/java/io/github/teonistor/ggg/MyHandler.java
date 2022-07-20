package io.github.teonistor.ggg;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification.S3EventNotificationRecord;
import org.springframework.web.client.RestTemplate;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class MyHandler implements RequestHandler<S3EventNotification, Void> {

    @Override
    public Void handleRequest(final S3EventNotification s3EventNotification, final Context context) {
        try {
            s3EventNotification.getRecords().stream()
                    .map(this::handleOne)
                    .reduce(ProcessingStats::plus)
                    .ifPresent(System.out::println);

        } catch (final Exception e) {
          e.printStackTrace();
        }
        return null;
    }

    private ProcessingStats handleOne(final S3EventNotificationRecord record) {
        final String inputFileName = record.getS3().getObject().getKey();
        final Optional<String> outputFileName = Optional.of(Pattern.compile("(.+)\\.kml").matcher(inputFileName))
                .filter(Matcher::matches)
                .map(m -> m.group(1) + ".html");
        if (outputFileName.isEmpty())
            return new ProcessingStats(0, 0, 1, 0);

        final String inputBucketName = record.getS3().getBucket().getName();
        final Optional<String> outputBucketName = Optional.of(Pattern.compile("(.+)-in").matcher(inputBucketName))
                .filter(Matcher::matches)
                .map(m -> m.group(1) + "-out");
        if (outputBucketName.isEmpty())
            return new ProcessingStats(0, 0, 0, 1);

        // Based on names like 20220604-160639 - Talbot, Poole circ.kml
        final S3Client s3 = S3Client.builder().build();
        final List<String> additionalFiles = Stream.of(Pattern.compile("(^.+?-)").matcher(inputFileName))
                .filter(Matcher::find)
                .map(m -> m.group(1))
                .flatMap(fileNamePrefix -> s3.listObjectsV2(ListObjectsV2Request.builder()
                                .bucket(inputBucketName)
                                .build())
                        .contents()
                        .stream()
                        .map(S3Object::key)
                        .filter(s -> s.startsWith(fileNamePrefix)))
                .collect(toList());

        System.out.println("Would process the following when implemented: " + additionalFiles);
        System.out.println("The current key as given: " + inputFileName);

        final String outputBucketNam = outputBucketName.get();
        final String outputFileNam = outputFileName.get();

        try (final ResponseInputStream<GetObjectResponse> response = s3.getObject(GetObjectRequest.builder()
                .bucket(inputBucketName)
                .key(inputFileName)
                .build())) {

            final String returned = new YourHandler().send(response);
            final RestTemplate rest = YourHandler.restTemplateBuilder.build();

            final String outputFileContents = Optional.of(Pattern.compile("a href=\"([^\"]*download[^\"]+)\"").matcher(returned))
                    .filter(Matcher::find)
                    .map(m -> m.group(1))
                    .map(m -> "https://www.gpsvisualizer.com" + (m.startsWith("/") ? "" : "/") + m)
                    .map(url -> rest.getForObject(url, String.class))
                    .orElse(returned);

            final PutObjectResponse putObjectResponse = s3.putObject(PutObjectRequest.builder()
                            .bucket(outputBucketNam)
                            .key(outputFileNam)
                            .build(),
                    RequestBody.fromString(outputFileContents));

            return new ProcessingStats(1, additionalFiles.size(), 0, 0);

        } catch (final IOException e) {
            e.printStackTrace();
            return new ProcessingStats(0, 0, 0, 1);
        }
    }
}
