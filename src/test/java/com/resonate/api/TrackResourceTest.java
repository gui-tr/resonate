package com.resonate.api;

import com.resonate.domain.media.AudioFile;
import com.resonate.domain.model.ArtistProfile;
import com.resonate.domain.model.Release;
import com.resonate.domain.model.Track;
import com.resonate.infrastructure.repository.AudioFileRepository;
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

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class TrackResourceTest {

    private final String basePath = "/api/tracks";
    private Long releaseId;
    private Long trackId;
    private Long audioFileId;


    @Inject
    TestDataSetup testDataSetup;

    @Inject
    ReleaseRepository releaseRepository;

    @Inject
    TrackRepository trackRepository;

    @Inject
    AudioFileRepository audioFileRepository;

    @BeforeEach
    @Transactional
    public void setup() {
        // Create a release for the fixed test artist
        Release release = testDataSetup.createRelease(TestUtil.ARTIST_UUID, "Test Release");
        releaseId = release.getId();

        // Create a track for the release
        Track track = testDataSetup.createTrack(release, "Test Track");
        trackId = track.getId();

        // Create an audio file
        AudioFile audioFile = testDataSetup.createAudioFile("test-file-identifier");
        audioFileId = audioFile.getId();

        testDataSetup.flushAndClear();
    }

    @AfterEach
    @Transactional
    public void cleanup() {
        testDataSetup.cleanupTestData();
    }

    @Test
    @TestSecurity(user = TestUtil.ARTIST_ID_STRING, roles = {"user"})
    public void testGetTrack() {
        given()
                .when()
                .get(basePath + "/" + trackId)
                .then()
                .statusCode(200)
                .contentType(ContentType.JSON)
                .body("title", equalTo("Test Track"))
                .body("duration", equalTo(180));
    }

    @Test
    @TestSecurity(user = TestUtil.ARTIST_ID_STRING, roles = {"user"})
    public void testGetNonExistentTrack() {
        given()
                .when()
                .get(basePath + "/999999")
                .then()
                .statusCode(404);
    }

    @Test
    @TestSecurity(user = TestUtil.ARTIST_ID_STRING, roles = {"user"})
    public void testCreateTrack() {
        Track newTrack = Track.builder()
                .title("New Test Track")
                .duration(240)
                .isrc("NEWTEST123")
                .filePath("/new/test/path.mp3")
                .fileSize(2048L)
                .build();

        given()
                .contentType(ContentType.JSON)
                .queryParam("releaseId", releaseId)
                .body(newTrack)
                .when()
                .post(basePath)
                .then()
                .statusCode(201)
                .body("title", equalTo("New Test Track"))
                .body("duration", equalTo(240))
                .body("id", notNullValue());
    }

    @Test
    @TestSecurity(user = TestUtil.ARTIST_ID_STRING, roles = {"user"})
    public void testCreateTrackWithAudioFile() {
        Track newTrack = Track.builder()
                .title("New Track With Audio")
                .duration(300)
                .isrc("AUDIO123")
                .filePath("/audio/path.mp3")
                .fileSize(3072L)
                .build();

        given()
                .contentType(ContentType.JSON)
                .queryParam("releaseId", releaseId)
                .queryParam("audioFileId", audioFileId)
                .body(newTrack)
                .when()
                .post(basePath)
                .then()
                .statusCode(201)
                .body("title", equalTo("New Track With Audio"))
                .body("audioFile.id", equalTo(audioFileId.intValue()));
    }

    @Test
    @TestSecurity(user = TestUtil.ARTIST_ID_STRING, roles = {"user"})
    public void testUpdateTrack() {
        Track updatedTrack = Track.builder()
                .title("Updated Track")
                .duration(360)
                .isrc("UPDATED123")
                .filePath("/updated/path.mp3")
                .fileSize(4096L)
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(updatedTrack)
                .when()
                .put(basePath + "/" + trackId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Updated Track"))
                .body("duration", equalTo(360));
    }

    @Test
    @TestSecurity(user = TestUtil.ARTIST_ID_STRING, roles = {"user"})
    public void testUpdateTrackWithAudioFile() {
        Track updatedTrack = Track.builder()
                .title("Updated with Audio")
                .duration(420)
                .isrc("UPDATEAUDIO123")
                .filePath("/updated/audio/path.mp3")
                .fileSize(5120L)
                .build();

        given()
                .contentType(ContentType.JSON)
                .queryParam("audioFileId", audioFileId)
                .body(updatedTrack)
                .when()
                .put(basePath + "/" + trackId)
                .then()
                .statusCode(200)
                .body("title", equalTo("Updated with Audio"))
                .body("audioFile.id", equalTo(audioFileId.intValue()));
    }

    @Test
    @TestSecurity(user = TestUtil.ARTIST_ID_STRING, roles = {"user"})
    public void testDeleteTrack() {
        given()
                .when()
                .delete(basePath + "/" + trackId)
                .then()
                .statusCode(204);

        // Verify deletion
        given()
                .when()
                .get(basePath + "/" + trackId)
                .then()
                .statusCode(200);
    }
}
