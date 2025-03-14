package com.resonate.api;

import com.resonate.domain.model.FanProfile;
import com.resonate.infrastructure.repository.FanProfileRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
public class FanProfileResourceTest {

    private final String basePath = "/api/fan-profiles";
    private static final String MOCK_USER_ID = "22222222-2222-2222-2222-222222222222";

    @Inject
    FanProfileRepository fanProfileRepository;

    @AfterEach
    @Transactional
    public void cleanup() {
        fanProfileRepository.deleteAll();
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testGetFanProfileNotFound() {
        given()
                .when().get(basePath)
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testCreateAndGetFanProfile() {
        FanProfile profile = FanProfile.builder()
                .subscriptionActive(true)
                .subscriptionStartDate(OffsetDateTime.now())
                .build();

        // Create (or update) profile via POST (upsert behavior)
        given()
                .contentType(ContentType.JSON)
                .body(profile)
                .when().post(basePath)
                .then()
                .statusCode(200)
                .body("subscriptionActive", equalTo(true));

        // Retrieve profile via GET
        given()
                .when().get(basePath)
                .then()
                .statusCode(200)
                .body("subscriptionActive", equalTo(true));
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testDeleteFanProfile() {
        FanProfile profile = FanProfile.builder()
                .subscriptionActive(true)
                .subscriptionStartDate(OffsetDateTime.now())
                .build();

        // Create profile
        given()
                .contentType(ContentType.JSON)
                .body(profile)
                .when().post(basePath)
                .then()
                .statusCode(200);

        // Delete profile
        given()
                .when().delete(basePath)
                .then()
                .statusCode(204);

        // Confirm deletion
        given()
                .when().get(basePath)
                .then()
                .statusCode(404);
    }
}
