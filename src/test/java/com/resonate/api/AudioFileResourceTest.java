package com.resonate.api;

import com.resonate.util.TestUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class AudioFileResourceTest {

    private final String basePath = "/api/audio-files";


    @Test
    @TestSecurity(user = TestUtil.ARTIST_ID_STRING, roles = {"user"})
    public void testGetSignedUploadUrl() {
        String fileName = "test-audio.mp3";

        given()
                .queryParam("fileName", fileName)
                .when()
                .get(basePath + "/upload")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                // Don't test exact URLs, just ensure fields exist
                .body("uploadUrl", notNullValue())
                .body("fileKey", notNullValue())
                .body("bucketName", notNullValue());
    }

    @Test
    @TestSecurity(user = TestUtil.ARTIST_ID_STRING, roles = {"user"})
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
