package com.resonate.api;

import com.resonate.domain.model.ArtistProfile;
import com.resonate.domain.model.Release;
import com.resonate.domain.model.Track;
import com.resonate.infrastructure.repository.ArtistProfileRepository;
import com.resonate.infrastructure.repository.ReleaseRepository;
import com.resonate.infrastructure.repository.TrackRepository;
import com.resonate.util.TestDataSetup;
import com.resonate.util.TestUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;

@QuarkusTest
public class ReleaseResourceTest {

    private final String basePath = "/api/releases";
    private static final String MOCK_USER_ID = TestUtil.ARTIST_ID_STRING;
    private UUID userId = UUID.fromString(MOCK_USER_ID);
    private Long releaseId;

    @Inject
    TestDataSetup testDataSetup;

    @Inject
    ArtistProfileRepository artistProfileRepository;

    @Inject
    ReleaseRepository releaseRepository;

    @Inject
    TrackRepository trackRepository;

    @BeforeEach
    public void setup() {
        // Create test data
        ArtistProfile artistProfile = testDataSetup.createArtistProfile(userId);
        Release release = testDataSetup.createRelease(userId, "Test Release");
        releaseId = release.getId();

        // Add a couple of tracks to the release
        Track track1 = testDataSetup.createTrack(release, "Track 1");
        Track track2 = testDataSetup.createTrack(release, "Track 2");
    }

    @AfterEach
    @Transactional
    public void cleanup() {
        trackRepository.deleteAll();
        releaseRepository.deleteAll();
        artistProfileRepository.deleteAll();
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testGetRelease() {
        given()
                .when()
                .get(basePath + "/" + releaseId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("title", equalTo("Test Release"))
                .body("artistId", equalTo(MOCK_USER_ID));
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testGetNonExistentRelease() {
        given()
                .when()
                .get(basePath + "/999999")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testCreateRelease() {
        Release newRelease = Release.builder()
                .title("New Test Release")
                .releaseDate(LocalDate.now())
                .upc("098765432109")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(newRelease)
                .when()
                .post(basePath)
                .then()
                .statusCode(201)
                .body("title", equalTo("New Test Release"))
                .body("id", notNullValue())
                .body("artistId", equalTo(MOCK_USER_ID));
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testUpdateRelease() {
        Release updatedRelease = Release.builder()
                .title("Updated Release Title")
                .releaseDate(LocalDate.now().plusDays(30))
                .upc("555555555555")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(updatedRelease)
                .when()
                .put(basePath + "/" + releaseId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Updated Release Title"))
                .body("upc", equalTo("555555555555"));
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testDeleteRelease() {
        given()
                .when()
                .delete(basePath + "/" + releaseId)
                .then()
                .statusCode(204);

        // Verify deletion
        given()
                .when()
                .get(basePath + "/" + releaseId)
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testGetAllReleases() {
        // Create another release to ensure we have multiple releases
        Release extraRelease = Release.builder()
                .title("Extra Release")
                .releaseDate(LocalDate.now().minusDays(10))
                .upc("999999999999")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(extraRelease)
                .when()
                .post(basePath)
                .then()
                .statusCode(201);

        // Test public releases endpoint
        given()
                .when()
                .get(basePath + "/public")
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(2))
                .body("find { it.title == 'Test Release' }.id", equalTo(releaseId.intValue()))
                .body("find { it.title == 'Extra Release' }.id", notNullValue());
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testGetPublicReleaseDetails() {
        given()
                .when()
                .get(basePath + "/public/" + releaseId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("release.title", equalTo("Test Release"))
                .body("tracks.size()", equalTo(2))
                .body("tracks.find { it.title == 'Track 1' }.id", notNullValue())
                .body("tracks.find { it.title == 'Track 2' }.id", notNullValue());
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testGetArtistReleases() {
        given()
                .when()
                .get(basePath)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("size()", greaterThanOrEqualTo(1))
                .body("find { it.title == 'Test Release' }.id", equalTo(releaseId.intValue()));
    }
}
