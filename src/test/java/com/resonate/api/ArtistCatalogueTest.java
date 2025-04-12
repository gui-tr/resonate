package com.resonate.api;

import com.resonate.domain.model.ArtistProfile;
import com.resonate.domain.model.Release;
import com.resonate.domain.model.Track;
import com.resonate.infrastructure.repository.ArtistProfileRepository;
import com.resonate.infrastructure.repository.ReleaseRepository;
import com.resonate.infrastructure.repository.TrackRepository;
import com.resonate.util.TestUtil;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import io.restassured.http.ContentType;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
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
        flushAndClear();
    }

    @Transactional
    void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    // Helper methods to reduce duplication
    private ArtistProfile createArtistProfile(String userId) {
        ArtistProfile artistProfile = ArtistProfile.builder()
                .userId(UUID.fromString(userId))
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

    private int createRelease(String title, String userId) {
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

    private int createTrack(int releaseId, String title, String userId) {
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
    @Transactional
    @TestSecurity(user = TestUtil.ARTIST_ID_STRING, roles = {"user"})
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
    @TestSecurity(user = TestUtil.ARTIST_ID_STRING, roles = {"user"})
    public void testCreateRelease() {
        createArtistProfile(TestUtil.ARTIST_ID_STRING);

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
    @TestSecurity(user = "random-user", roles = {"user"})
    public void testUpdateRelease() {
        String userId = TestUtil.genUUID();
        createArtistProfile(userId);
        int releaseId = createRelease("Original Release", userId);

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
    @TestSecurity(user = "random-user", roles = {"user"})
    public void testCreateTrack() {
        String userId = TestUtil.genUUID();
        createArtistProfile(userId);
        int releaseId = createRelease("Release for Track", userId);

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
    @Transactional
    @TestSecurity(user = "random-user", roles = {"user"})
    public void testUpdateTrack() {
        String userId = TestUtil.genUUID();
        createArtistProfile(userId);
        int releaseId = createRelease("Release for Track Update", userId);
        int trackId = createTrack(releaseId, "Original Track", userId);

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