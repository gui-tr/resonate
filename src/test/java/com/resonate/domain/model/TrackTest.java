package com.resonate.domain.model;

import com.resonate.domain.media.AudioFile;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.OffsetDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@QuarkusTest
class TrackTest {

    private Release mockRelease;
    private AudioFile mockAudioFile;

    @BeforeEach
    void setUp() {
        mockRelease = mock(Release.class);
        mockAudioFile = mock(AudioFile.class);
    }

    @Test
    void testTrackCreationWithBuilder() {
        String title = "Test Track";
        int duration = 180;
        String isrc = "US-123-45-67890";
        String filePath = "/path/to/track.mp3";
        Long fileSize = 1024L;

        Track track = Track.builder()
                .title(title)
                .duration(duration)
                .release(mockRelease)
                .build();

        assertNotNull(track);
        assertEquals(title, track.getTitle());
        assertEquals(duration, track.getDuration());

        assertEquals(mockRelease, track.getRelease());

    }

    @Test
    void testTrackCreationWithNoArgsConstructor() {
        Track track = new Track();

        assertNotNull(track);
        assertNull(track.getTitle());
        assertEquals(0, track.getDuration());

        assertNull(track.getRelease());

    }

    @Test
    void testTrackCreationWithAllArgsConstructor() {
        Long id = 1L;
        String title = "Test Track";
        int duration = 180;
        String isrc = "US-123-45-67890";
        String filePath = "/path/to/track.mp3";
        Long fileSize = 1024L;
        OffsetDateTime createdAt = OffsetDateTime.now();

        Track track = new Track(id, title, duration, mockRelease);

        assertNotNull(track);
        assertEquals(id, track.getId());
        assertEquals(title, track.getTitle());
        assertEquals(duration, track.getDuration());

    }

    @Test
    void testTrackSettersAndGetters() {
        Track track = new Track();

        track.setTitle("New Title");
        track.setDuration(200);

        track.setRelease(mockRelease);
        OffsetDateTime now = OffsetDateTime.now();

        assertEquals("New Title", track.getTitle());
        assertEquals(200, track.getDuration());

        assertEquals(mockRelease, track.getRelease());

    }
} 