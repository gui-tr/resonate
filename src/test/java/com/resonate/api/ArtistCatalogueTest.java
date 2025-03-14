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
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

@QuarkusTest
public class ArtistCatalogueTest {

    private final String artistProfilePath = "/api/artist-profiles";
    private final String releasePath = "/api/releases";
    private final String trackPath = "/api/tracks";

    private static final String MOCK_USER_ID = "11111111-1111-1111-1111-111111111111";

    @Inject
    ArtistProfileRepository artistProfileRepository;

    @Inject
    ReleaseRepository releaseRepository;

    @Inject
    TrackRepository trackRepository;

    @AfterEach
    @Transactional
    public void cleanup() {
        // Clean up all tables so tests remain isolated
        trackRepository.deleteAll();
        releaseRepository.deleteAll();
        artistProfileRepository.deleteAll();
    }

    @Test
    @TestSecurity(user = MOCK_USER_ID, roles = {"user"})
    public void testArtistCatalogueFlow() {

        // 1: Create a new artist profile
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

        // 2: Create a new release
        Release release = Release.builder()
                .title("New Release")
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
                .body("title", equalTo("New Release"))
                .extract().path("id");

        Track track = Track.builder()
                .title("New Track")
                .duration(200)
                .isrc("ISRC123456")
                .filePath("path/to/track.mp3")
                .fileSize(5000000L)
                .build();

        int trackId = given()
                .contentType(ContentType.JSON)
                .queryParam("releaseId", releaseId)
                .body(track)
                .when()
                .post(trackPath)
                .then()
                .statusCode(201)
                .body("title", equalTo("New Track"))
                .extract().path("id");

        // 4: Verify that the release is linked to the artist catalogue

        given()
                .when().get(releasePath + "/" + releaseId)
                .then()
                .statusCode(200)
                .body("artistId", equalTo(MOCK_USER_ID));
    }
}
