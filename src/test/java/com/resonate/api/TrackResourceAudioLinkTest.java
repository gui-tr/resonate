package com.resonate.api;

import com.resonate.domain.media.AudioFile;
import com.resonate.domain.model.ArtistProfile;
import com.resonate.domain.model.Release;
import com.resonate.domain.model.Track;
import com.resonate.infrastructure.repository.ArtistProfileRepository;
import com.resonate.infrastructure.repository.ReleaseRepository;
import com.resonate.infrastructure.repository.TrackRepository;
import com.resonate.infrastructure.repository.AudioFileRepository;
import com.resonate.util.TestUtil;
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
import static org.hamcrest.CoreMatchers.notNullValue;

@QuarkusTest
public class TrackResourceAudioLinkTest {

    private final String artistProfilePath = "/api/artist-profiles";
    private final String releaseBasePath = "/api/releases";
    private final String trackBasePath = "/api/tracks";

    private static final String MOCK_USER_ID = TestUtil.ARTIST_ID_STRING;


    @Inject
    ArtistProfileRepository artistProfileRepository;

    @Inject
    ReleaseRepository releaseRepository;

    @Inject
    TrackRepository trackRepository;

    @Inject
    AudioFileRepository audioFileRepository;

    @AfterEach
    @Transactional
    public void cleanup() {
        trackRepository.deleteAll();
        releaseRepository.deleteAll();
        audioFileRepository.deleteAll();
        artistProfileRepository.deleteAll();
    }


    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testCreateTrackWithAudioFileLink() {
        // 0. Create an ArtistProfile to satisfy the foreign key constraint on releases.
        ArtistProfile artistProfile = ArtistProfile.builder()
                .biography("Test Artist")
                .socialLinks("{\"twitter\":\"@artist\"}")
                .build();

        given()
                .contentType(ContentType.JSON)
                .body(artistProfile)
                .when()
                .post(artistProfilePath)
                .then()
                .statusCode(200)
                .body("biography", equalTo("Test Artist"));

        // 1. Create a release.
        Release release = Release.builder()
                .title("Release for AudioLink")
                .releaseDate(LocalDate.now())
                .upc("111111111111")
                .build();

        int releaseId = given()
                .contentType(ContentType.JSON)
                .body(release)
                .when().post(releaseBasePath)
                .then()
                .statusCode(201)
                .body("title", equalTo("Release for AudioLink"))
                .extract().path("id");

        // 2. Create a dummy AudioFileEntity via helper method.
        AudioFile audioFile = AudioFile.builder()
                .fileIdentifier("dummy-file-id")
                .fileUrl("https://dummyurl.com/audio.mp3")
                .fileSize(123456L)
                .checksum("dummy-checksum")
                .build();
        Long audioFileId = saveAudioFile(audioFile);

        // 3. Create a track, linking it to the audio file by passing its ID as a query parameter.
        Track track = Track.builder()
                .title("Track with Audio Link")
                .duration(180)
                .isrc("AUDIOISRC")
                .filePath("unused-for-now")
                .fileSize(0L)
                .build();

        int trackId = given()
                .contentType(ContentType.JSON)
                .queryParam("releaseId", releaseId)
                .queryParam("audioFileId", audioFileId)
                .body(track)
                .when().post(trackBasePath)
                .then()
                .statusCode(201)
                .body("title", equalTo("Track with Audio Link"))
                .body("audioFile.id", notNullValue())
                .extract().path("id");

        // 4. Retrieve the track and verify the audio file link.
        given()
                .when().get(trackBasePath + "/" + trackId)
                .then()
                .statusCode(200)
                .body("audioFile.id", equalTo(audioFileId.intValue()));
    }


    // Helper method to persist audio file in its own transaction.
    @Transactional
    public Long saveAudioFile(AudioFile audioFile) {
        audioFileRepository.persist(audioFile);
        audioFileRepository.flush();
        return audioFile.getId();
    }
}
