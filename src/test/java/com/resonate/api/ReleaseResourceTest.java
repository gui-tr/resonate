package com.resonate.api;

import com.resonate.domain.model.Release;
import com.resonate.infrastructure.repository.ReleaseRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
public class ReleaseResourceTest {

    private final String basePath = "/api/releases";
    private static final String MOCK_USER_ID = "11111111-1111-1111-1111-111111111111";

    @Inject
    ReleaseRepository releaseRepository;

    @AfterEach
    @Transactional
    public void cleanup() {
        releaseRepository.deleteAll();
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testCreateAndGetRelease() {
        Release release = Release.builder()
                .title("Test Release")
                .releaseDate(LocalDate.now())
                .upc("123456789012")
                .build();

        int releaseId = given()
                .contentType(ContentType.JSON)
                .body(release)
                .when().post(basePath)
                .then()
                .statusCode(201)
                .body("title", equalTo("Test Release"))
                .extract().path("id");

        given()
                .when().get(basePath + "/" + releaseId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Test Release"));
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testUpdateRelease() {
        // Create initial release
        Release release = Release.builder()
                .title("Initial Title")
                .releaseDate(LocalDate.now())
                .upc("987654321098")
                .build();

        Release created = given()
                .contentType(ContentType.JSON)
                .body(release)
                .when().post(basePath)
                .then()
                .statusCode(201)
                .extract().as(Release.class);

        // Prepare update payload
        Release updatedRelease = Release.builder()
                .title("Updated Title")
                .releaseDate(LocalDate.now())
                .upc("987654321098")
                .build();

        Release updated = given()
                .contentType(ContentType.JSON)
                .body(updatedRelease)
                .when().put(basePath + "/" + created.getId())
                .then()
                .statusCode(200)
                .body("title", equalTo("Updated Title"))
                .extract().as(Release.class);
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testDeleteRelease() {
        // Create a release
        Release release = Release.builder()
                .title("To be deleted")
                .releaseDate(LocalDate.now())
                .upc("000000000000")
                .build();

        Release created = given()
                .contentType(ContentType.JSON)
                .body(release)
                .when().post(basePath)
                .then()
                .statusCode(201)
                .extract().as(Release.class);

        // Delete the release
        given()
                .when().delete(basePath + "/" + created.getId())
                .then().statusCode(204);

    }
}
