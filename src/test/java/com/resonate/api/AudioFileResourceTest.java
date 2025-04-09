package com.resonate.api;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class AudioFileResourceTest {

    private final String basePath = "/api/audio-files";

    private static final String MOCK_USER_ID = "11111111-1111-1111-1111-111111111111";

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testGetSignedUploadUrl() {
        String fileName = "test-audio.mp3";

        given()
                .queryParam("fileName", fileName)
                .when()
                .get(basePath + "/upload")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                // Update to match our mock response, checking only that required fields exist
                .body("uploadUrl", notNullValue())
                .body("fileKey", notNullValue())
                .body("bucketName", notNullValue());
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testRegisterAudioFile() {
        AudioFileResource.AudioFileRegistration registration = new AudioFileResource.AudioFileRegistration();
        registration.fileKey = "test-file-key";
        registration.fileSize = 1024L;
        registration.checksum = "test-checksum";

        given()
                .contentType(ContentType.JSON)
                .body(registration)
                .when()
                .post(basePath + "/register")
                .then()
                .statusCode(201)
                .body("id", notNullValue())
                .body("fileIdentifier", notNullValue())
                .body("fileUrl", notNullValue());
    }
}
