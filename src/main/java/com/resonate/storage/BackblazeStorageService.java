package com.resonate.storage;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jboss.logging.Logger;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadBucketRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@ApplicationScoped
public class BackblazeStorageService {
    private static final Logger LOG = Logger.getLogger(BackblazeStorageService.class);

    private S3Client s3Client;
    private S3Presigner presigner;
    private boolean initialized = false;

    @ConfigProperty(name = "backblaze.keyId")
    String keyId;

    @ConfigProperty(name = "backblaze.applicationKey")
    String applicationKey;

    @ConfigProperty(name = "backblaze.bucketName")
    String bucketName;

    @ConfigProperty(name = "backblaze.endpoint", defaultValue = "https://s3.us-west-001.backblazeb2.com")
    String endpoint;


    void onStart(@Observes StartupEvent ev) {
        initClients();
        validateConnection();
    }

    private void initClients() {
        try {
            LOG.info("Initializing Backblaze B2 storage client");

            AwsBasicCredentials credentials = AwsBasicCredentials.create(keyId, applicationKey);

            // Region doesn't matter for Backblaze, but it's required for the AWS SDK
            s3Client = S3Client.builder()
                    .region(Region.EU_CENTRAL_1)
                    .endpointOverride(java.net.URI.create(endpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            presigner = S3Presigner.builder()
                    .region(Region.EU_CENTRAL_1)
                    .endpointOverride(java.net.URI.create(endpoint))
                    .credentialsProvider(StaticCredentialsProvider.create(credentials))
                    .build();

            initialized = true;
            LOG.info("Backblaze B2 storage client initialized successfully");
        } catch (Exception e) {
            LOG.error("Failed to initialize Backblaze B2 storage client", e);
            throw new RuntimeException("Failed to initialize storage service", e);
        }
    }

    private void validateConnection() {
        if (!initialized) {
            LOG.warn("Cannot validate connection: client not initialized");
            return;
        }

        try {
            LOG.info("Validating connection to Backblaze B2 bucket: " + bucketName);

            HeadBucketRequest headBucketRequest = HeadBucketRequest.builder()
                    .bucket(bucketName)
                    .build();

            s3Client.headBucket(headBucketRequest);
            LOG.info("Successfully connected to Backblaze B2 bucket: " + bucketName);
        } catch (SdkException e) {
            LOG.error("Failed to connect to Backblaze B2 bucket: " + bucketName, e);
            throw new RuntimeException("Failed to connect to storage bucket", e);
        }
    }

    /**
     * Generates a signed URL for uploading a file to Backblaze B2.
     *
     * @param fileName Original file name (will be prefixed with a UUID)
     * @param contentType MIME type of the file
     * @return Map containing the upload URL, file key, and bucket name
     */
    public Map<String, String> generateUploadUrl(String fileName, String contentType) {
        if (!initialized) {
            throw new IllegalStateException("Storage service not properly initialized");
        }

        String key = UUID.randomUUID() + "-" + fileName;
        LOG.debug("Generating upload URL for file: " + key);

        try {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(10))
                    .putObjectRequest(objectRequest)
                    .build();

            String uploadUrl = presigner.presignPutObject(presignRequest).url().toString();
            LOG.debug("Generated upload URL: " + uploadUrl);

            Map<String, String> result = new HashMap<>();
            result.put("uploadUrl", uploadUrl);
            result.put("fileKey", key);
            result.put("bucketName", bucketName);

            return result;
        } catch (Exception e) {
            LOG.error("Failed to generate upload URL", e);
            throw new RuntimeException("Failed to generate upload URL", e);
        }
    }

    /**
     * Generates a signed URL for downloading/streaming a file from Backblaze B2.
     *
     * @param fileKey The file identifier in Backblaze B2
     * @return Signed URL for downloading/streaming the file
     */
    public String generateDownloadUrl(String fileKey) {
        if (!initialized) {
            throw new IllegalStateException("Storage service not properly initialized");
        }

        LOG.debug("Generating download URL for file: " + fileKey);

        try {
            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(60))
                    .getObjectRequest(r -> r.bucket(bucketName).key(fileKey))
                    .build();

            String downloadUrl = presigner.presignGetObject(presignRequest).url().toString();
            LOG.debug("Generated download URL: " + downloadUrl);

            return downloadUrl;
        } catch (Exception e) {
            LOG.error("Failed to generate download URL for file: " + fileKey, e);
            throw new RuntimeException("Failed to generate download URL", e);
        }
    }
}
