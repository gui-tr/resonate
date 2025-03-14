package com.resonate.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.resonate.domain.model.ArtistProfile;
import com.resonate.infrastructure.repository.ArtistProfileRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;



import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
public class ArtistProfileResourceTest {

    private final String basePath = "/api/artist-profiles";
    private final String socialLinks = "{\"twitter\":\"@artist\"}";
    private static final String MOCK_USER_ID = "11111111-1111-1111-1111-111111111111";

    @Inject
    ArtistProfileRepository artistProfileRepository;


    @AfterEach
    @Transactional
    public void cleanup() {
        artistProfileRepository.deleteAll();
    }


    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testGetProfileNotFound() {
        given()
                .when().get(basePath)
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testCreateAndGetArtistProfile() throws JsonProcessingException {

        ArtistProfile profile = ArtistProfile.builder()
                .biography("Test bio")
                .socialLinks(socialLinks)
                .build();

        // Create profile
        given()
                .contentType(ContentType.JSON)
                .body(profile)
                .when().post(basePath)
                .then()
                .statusCode(200)
                .body("biography", equalTo("Test bio"));

        // Retrieve profile
        given()
                .when().get(basePath)
                .then()
                .statusCode(200)
                .body("biography", equalTo("Test bio"));
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testDeleteArtistProfile() throws JsonProcessingException {

        ArtistProfile profile = ArtistProfile.builder()
                .biography("Test delete")
                .socialLinks(socialLinks)
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
