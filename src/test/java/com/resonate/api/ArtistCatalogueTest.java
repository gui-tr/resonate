package com.resonate.api;

import com.resonate.domain.model.ArtistProfile;
import com.resonate.domain.model.Release;
import com.resonate.domain.model.Track;
import com.resonate.infrastructure.repository.ArtistProfileRepository;
import com.resonate.infrastructure.repository.ReleaseRepository;
import com.resonate.infrastructure.repository.TrackRepository;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class ArtistCatalogueTest {

    private final String artistProfilePath = "/api/artist-profiles";
    private final String releasePath = "/api/releases";
    private final String trackPath = "/api/tracks";
    private static final String MOCK_USER_ID = "11111111-1111-1111-1111-111111111111";

    @Inject
    EntityManager entityManager;

    @Inject
    ArtistProfileRepository artistProfileRepository;

    @Inject
    ReleaseRepository releaseRepository;

    @Inject
    TrackRepository trackRepository;

    @AfterEach
    @Transactional
    public void cleanup() {
        trackRepository.deleteAll();
        releaseRepository.deleteAll();
        artistProfileRepository.deleteAll();
    }

    @Transactional
    void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    // Helper methods to reduce duplication
    private ArtistProfile createArtistProfile() {
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

        flushAndClear();
        return artistProfile;
    }

    private int createRelease(String title) {
        Release release = Release.builder()
                .title(title)
                .releaseDate(LocalDate.now())
                .upc("123456789012")
                .build();

        int releaseId = given()
                .contentType(ContentType.JSON)
                .body(release)
                .when()
                .post(releasePath)
                .then()
                .statusCode(201)
                .body("title", equalTo(title))
                .extract().path("id");

        flushAndClear();
        return releaseId;
    }

    private int createTrack(int releaseId, String title) {
        Track track = Track.builder()
                .title(title)
                .duration(200)
                .isrc("TEST123")
                .filePath("test/path.mp3")
                .fileSize(100000L)
                .build();

        int trackId = given()
                .contentType(ContentType.JSON)
                .queryParam("releaseId", releaseId)
                .body(track)
                .when()
                .post(trackPath)
                .then()
                .statusCode(201)
                .body("title", equalTo(title))
                .extract().path("id");

        flushAndClear();
        return trackId;
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testCreateArtistProfile() {
        // Create an Artist Profile
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
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testCreateRelease() {
        // Create an artist profile first
        createArtistProfile();

        // Create a new Release
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
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testUpdateRelease() {
        // Create prerequisites
        createArtistProfile();
        int releaseId = createRelease("Original Release");

        // Update the Release
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
                .statusCode(200)
                .body("title", equalTo("Updated Release"));
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testCreateTrack() {
        // Create prerequisites
        createArtistProfile();
        int releaseId = createRelease("Release for Track");

        // Create a new Track
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
                .statusCode(201)
                .body("title", equalTo("New Track"))
                .body("id", notNullValue());
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testUpdateTrack() {
        // Create prerequisites
        createArtistProfile();
        int releaseId = createRelease("Release for Track Update");
        int trackId = createTrack(releaseId, "Original Track");

        // Update the Track
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
                .statusCode(200)
                .body("title", equalTo("Updated Track"));
    }

}