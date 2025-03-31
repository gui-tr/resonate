package com.resonate.api;

import com.resonate.domain.model.Release;
import com.resonate.domain.model.Track;
import com.resonate.infrastructure.repository.ReleaseRepository;
import com.resonate.infrastructure.repository.TrackRepository;
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
public class TrackResourceTest {

    private final String releaseBasePath = "/api/releases";
    private final String trackBasePath = "/api/tracks";
    private static final String MOCK_USER_ID = "11111111-1111-1111-1111-111111111111";

    @Inject
    ReleaseRepository releaseRepository;

    @Inject
    TrackRepository trackRepository;

    @AfterEach
    @Transactional
    public void cleanup() {
        trackRepository.deleteAll();
        releaseRepository.deleteAll();
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testUpdateTrack() {
        // First, create a release to satisfy the FK constraint.
        Release release = Release.builder()
                .title("Release for Update Track")
                .releaseDate(LocalDate.now())
                .upc("222222222222")
                .build();

        Release createdRelease = given()
                .contentType(ContentType.JSON)
                .body(release)
                .when().post(releaseBasePath)
                .then()
                .statusCode(201)
                .extract().as(Release.class);

        // Create a track for that release.
        Track track = Track.builder()
                .title("Initial Track")
                .release(createdRelease)
                .duration(200)
                .isrc("INIT123")
                .filePath("initial/path.mp3")
                .fileSize(100000L)
                .build();

        Track createdTrack = given()
                .contentType(ContentType.JSON)
                .queryParam("releaseId", createdRelease.getId())
                .body(track)
                .when().post(trackBasePath)
                .then()
                .statusCode(201)
                .extract().as(Track.class);

        // Prepare an updated track object.
        Track updatedTrack = Track.builder()
                .title("Updated Track")
                .duration(240)
                .isrc("UPDATED123")
                .filePath("updated/path.mp3")
                .fileSize(200000L)
                .build();

        Track resultTrack = given()
                .contentType(ContentType.JSON)
                .body(updatedTrack)
                .when().put(trackBasePath + "/" + createdTrack.getId())
                .then()
                .statusCode(200)
                .body("title", equalTo("Updated Track"))
                .extract().as(Track.class);
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testDeleteTrack() {
        // Create a release first.
        Release release = Release.builder()
                .title("Release for Delete Track")
                .releaseDate(LocalDate.now())
                .upc("333333333333")
                .build();

        Release createdRelease = given()
                .contentType(ContentType.JSON)
                .body(release)
                .when().post(releaseBasePath)
                .then()
                .statusCode(201)
                .extract().as(Release.class);

        // Create a track.
        Track track = Track.builder()
                .title("Track to Delete")
                .release(createdRelease)
                .duration(150)
                .isrc("DEL123")
                .filePath("delete/path.mp3")
                .fileSize(300000L)
                .build();

        Track createdTrack = given()
                .contentType(ContentType.JSON)
                .queryParam("releaseId", createdRelease.getId())
                .body(track)
                .when().post(trackBasePath)
                .then()
                .statusCode(201)
                .extract().as(Track.class);

        // Delete the track.
        given()
                .when().delete(trackBasePath + "/" + createdTrack.getId())
                .then().statusCode(204);

    }
}
