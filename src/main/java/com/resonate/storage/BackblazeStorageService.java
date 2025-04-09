package com.resonate.storage;

import io.quarkus.runtime.StartupEvent;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
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

    private S3Client s3Client;
    private S3Presigner presigner;

    @ConfigProperty(name = "backblaze.keyId", defaultValue = "")
    String keyId;

    @ConfigProperty(name = "backblaze.applicationKey", defaultValue = "")
    String applicationKey;

    @ConfigProperty(name = "backblaze.bucketName", defaultValue = "")
    String bucketName;

    @ConfigProperty(name = "backblaze.endpoint", defaultValue = "https://s3.us-west-001.backblazeb2.com")
    String endpoint;

    void onStart(@Observes StartupEvent ev) {
        if (keyId != null && !keyId.isEmpty() && applicationKey != null && !applicationKey.isEmpty()) {
            initClients();
        }
    }

    private void initClients() {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(keyId, applicationKey);

        s3Client = S3Client.builder()
                .region(Region.EU_CENTRAL_1) // Backblaze doesn't use AWS regions but requires one
                .endpointOverride(java.net.URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();

        presigner = S3Presigner.builder()
                .region(Region.EU_CENTRAL_1)
                .endpointOverride(java.net.URI.create(endpoint))
                .credentialsProvider(StaticCredentialsProvider.create(credentials))
                .build();
    }

    public Map<String, String> generateUploadUrl(String fileName, String contentType) {
        if (presigner == null) {
            return generateMockUploadUrl(fileName);
        }

        String key = UUID.randomUUID() + "-" + fileName;

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

        Map<String, String> result = new HashMap<>();
        result.put("uploadUrl", uploadUrl);
        result.put("fileKey", key);
        result.put("bucketName", bucketName);

        return result;
    }

    public String generateDownloadUrl(String fileKey) {
        if (presigner == null) {
            return generateMockDownloadUrl(fileKey);
        }

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(60))
                .getObjectRequest(r -> r.bucket(bucketName).key(fileKey))
                .build();

        return presigner.presignGetObject(presignRequest).url().toString();
    }

    // Mock implementations for development without actual Backblaze credentials
    private Map<String, String> generateMockUploadUrl(String fileName) {
        Map<String, String> result = new HashMap<>();
        String fileKey = UUID.randomUUID() + "-" + fileName;
        result.put("uploadUrl", "https://mock-backblaze-upload.example.com/" + fileKey);
        result.put("fileKey", fileKey);
        result.put("bucketName", "mock-bucket");
        return result;
    }

    private String generateMockDownloadUrl(String fileKey) {
        return "https://mock-backblaze-download.example.com/" + fileKey;
    }
}