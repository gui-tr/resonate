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
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class ArtistCatalogueTest {

    private final String artistProfilePath = "/api/artist-profiles";
    private final String releasePath = "/api/releases";
    private final String trackPath = "/api/tracks";
    private static final String MOCK_USER_ID = TestUtil.ARTIST_ID_STRING;
    private static final String UNAUTHORIZED_USER_ID = TestUtil.FAN_ID_STRING;

    @Inject
    EntityManager entityManager;

    @Inject
    TestDataSetup testDataSetup;

    @Inject
    ArtistProfileRepository artistProfileRepository;

    @Inject
    ReleaseRepository releaseRepository;

    @Inject
    TrackRepository trackRepository;

    private Long releaseId;
    private Long trackId;

    @BeforeEach
    @Transactional
    public void setup() {
        // Create test data using TestDataSetup
        ArtistProfile artistProfile = testDataSetup.createArtistProfile(TestUtil.ARTIST_UUID);
        Release release = testDataSetup.createRelease(TestUtil.ARTIST_UUID, "Test Release");
        releaseId = release.getId();
        Track track = testDataSetup.createTrack(release, "Test Track");
        trackId = track.getId();
        testDataSetup.flushAndClear();
    }

    @AfterEach
    @Transactional
    public void cleanup() {
        testDataSetup.cleanupTestData();
    }

    @Test
    @Transactional
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testCreateArtistProfile() {
        ArtistProfile artistProfile = ArtistProfile.builder()
                .biography("Artist Biography")
                .socialLinks("{\"twitter\":\"@artist\"}")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(artistProfile)
                .when()
                .post(artistProfilePath)
                .then()
                .statusCode(200)
                .body("biography", equalTo("Artist Biography"));
    }

    @Test
    @Transactional
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testCreateRelease() {
        Release release = Release.builder()
                .title("Test Release")
                .releaseDate(LocalDate.now())
                .upc("123456789012")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(release)
                .when()
                .post(releasePath)
                .then()
                .statusCode(201)
                .body("title", equalTo("Test Release"))
                .body("id", notNullValue());
    }

    @Test
    @Transactional
    @TestSecurity(user = UNAUTHORIZED_USER_ID, roles = {"user"})
    public void testUpdateRelease() {
        Release updatedRelease = Release.builder()
                .title("Updated Release")
                .releaseDate(LocalDate.now())
                .upc("123456789012")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(updatedRelease)
                .when()
                .put(releasePath + "/" + releaseId)
                .then()
                .statusCode(403);
    }

    @Test
    @Transactional
    @TestSecurity(user = UNAUTHORIZED_USER_ID, roles = {"user"})
    public void testCreateTrack() {
        Track track = Track.builder()
                .title("New Track")
                .duration(180)
                .isrc("TRACK123")
                .filePath("tracks/new.mp3")
                .fileSize(150000L)
                .build();

        given()
                .contentType(ContentType.JSON)
                .queryParam("releaseId", releaseId)
                .body(track)
                .when()
                .post(trackPath)
                .then()
                .statusCode(403);
    }

    @Test
    @Transactional
    @TestSecurity(user = UNAUTHORIZED_USER_ID, roles = {"user"})
    public void testUpdateTrack() {
        Track updatedTrack = Track.builder()
                .title("Updated Track")
                .duration(240)
                .isrc("UPDATED123")
                .filePath("tracks/updated.mp3")
                .fileSize(200000L)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(updatedTrack)
                .when()
                .put(trackPath + "/" + trackId)
                .then()
                .statusCode(403);
    }
}