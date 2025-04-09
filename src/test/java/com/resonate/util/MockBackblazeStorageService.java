
package com.resonate.util;

import com.resonate.storage.BackblazeStorageService;
import io.quarkus.test.Mock;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

@Mock
@ApplicationScoped
public class MockBackblazeStorageService extends BackblazeStorageService {

    @Override
    public Map<String, String> generateUploadUrl(String fileName, String contentType) {
        Map<String, String> mockResult = new HashMap<>();
        mockResult.put("uploadUrl", "https://f001.backblazeb2.com/file/test-bucket/" + fileName);
        mockResult.put("fileKey", "mock-key-" + fileName);
        mockResult.put("bucketName", "test-bucket");
        return mockResult;
    }

    @Override
    public String generateDownloadUrl(String fileKey) {
        return "https://f001.backblazeb2.com/file/test-bucket/" + fileKey;
    }
}
