package com.resonate.domain.model;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@QuarkusTest
class ReleaseTest {

    private Track mockTrack1;
    private Track mockTrack2;
    private UUID mockArtistId;

    @BeforeEach
    void setUp() {
        mockTrack1 = mock(Track.class);
        mockTrack2 = mock(Track.class);
        mockArtistId = UUID.randomUUID();
    }

    @Test
    void testReleaseCreationWithBuilder() {
        String title = "Test Release";
        LocalDate releaseDate = LocalDate.now();
        String upc = "123456789012";
        List<Track> tracks = Arrays.asList(mockTrack1, mockTrack2);

        Release release = Release.builder()
                .title(title)
                .artistId(mockArtistId)
                .releaseDate(releaseDate)
                .upc(upc)
                .tracks(tracks)
                .build();

        assertNotNull(release);
        assertEquals(title, release.getTitle());
        assertEquals(mockArtistId, release.getArtistId());
        assertEquals(releaseDate, release.getReleaseDate());
        assertEquals(upc, release.getUpc());
        assertEquals(tracks, release.getTracks());
        assertNotNull(release.getCreatedAt());
    }

    @Test
    void testReleaseCreationWithNoArgsConstructor() {
        Release release = new Release();

        assertNotNull(release);
        assertNull(release.getTitle());
        assertNull(release.getArtistId());
        assertNull(release.getReleaseDate());
        assertNull(release.getUpc());
        assertNotNull(release.getTracks());
        assertTrue(release.getTracks().isEmpty());
        assertNotNull(release.getCreatedAt());
    }

    @Test
    void testReleaseCreationWithAllArgsConstructor() {
        Long id = 1L;
        String title = "Test Release";
        LocalDate releaseDate = LocalDate.now();
        String upc = "123456789012";
        List<Track> tracks = Arrays.asList(mockTrack1, mockTrack2);
        OffsetDateTime createdAt = OffsetDateTime.now();

        Release release = new Release(id, mockArtistId, title, releaseDate, upc, createdAt, tracks);

        assertNotNull(release);
        assertEquals(id, release.getId());
        assertEquals(title, release.getTitle());
        assertEquals(mockArtistId, release.getArtistId());
        assertEquals(releaseDate, release.getReleaseDate());
        assertEquals(upc, release.getUpc());
        assertEquals(tracks, release.getTracks());
        assertEquals(createdAt, release.getCreatedAt());
    }

    @Test
    void testDefaultCreatedAtValue() {
        Release release = Release.builder()
                .title("Test Release")
                .artistId(mockArtistId)
                .releaseDate(LocalDate.now())
                .build();

        assertNotNull(release.getCreatedAt());
        assertTrue(release.getCreatedAt().isBefore(OffsetDateTime.now().plusSeconds(1)));
    }

    @Test
    void testReleaseSettersAndGetters() {
        Release release = new Release();
        String newTitle = "New Release";
        UUID newArtistId = UUID.randomUUID();
        LocalDate newReleaseDate = LocalDate.now().plusDays(1);
        String newUpc = "987654321098";
        List<Track> newTracks = Arrays.asList(mockTrack1);
        OffsetDateTime now = OffsetDateTime.now();

        release.setTitle(newTitle);
        release.setArtistId(newArtistId);
        release.setReleaseDate(newReleaseDate);
        release.setUpc(newUpc);
        release.setTracks(newTracks);
        release.setCreatedAt(now);

        assertEquals(newTitle, release.getTitle());
        assertEquals(newArtistId, release.getArtistId());
        assertEquals(newReleaseDate, release.getReleaseDate());
        assertEquals(newUpc, release.getUpc());
        assertEquals(newTracks, release.getTracks());
        assertEquals(now, release.getCreatedAt());
    }

    @Test
    void testTrackListModification() {
        Release release = Release.builder()
                .title("Test Release")
                .artistId(mockArtistId)
                .releaseDate(LocalDate.now())
                .build();

        release.getTracks().add(mockTrack1);
        release.getTracks().add(mockTrack2);

        assertEquals(2, release.getTracks().size());
        assertTrue(release.getTracks().contains(mockTrack1));
        assertTrue(release.getTracks().contains(mockTrack2));
    }
} 