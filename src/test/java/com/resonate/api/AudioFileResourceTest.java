package com.resonate.api;

import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.startsWith;

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
                // Check that a signedUrl field is returned, and it starts with the expected bucket URL prefix.
                .body("signedUrl", notNullValue())
                .body("signedUrl", startsWith("https://f001.backblazeb2.com/file/"));
    }
}
