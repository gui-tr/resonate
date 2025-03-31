package com.resonate.domain.model;

import com.resonate.domain.media.AudioFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import java.time.OffsetDateTime;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

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
        // Given
        String title = "Test Track";
        int duration = 180;
        String isrc = "US-123-45-67890";
        String filePath = "/path/to/track.mp3";
        Long fileSize = 1024L;

        // When
        Track track = Track.builder()
                .title(title)
                .duration(duration)
                .isrc(isrc)
                .filePath(filePath)
                .fileSize(fileSize)
                .release(mockRelease)
                .audioFile(mockAudioFile)
                .build();

        // Then
        assertNotNull(track);
        assertEquals(title, track.getTitle());
        assertEquals(duration, track.getDuration());
        assertEquals(isrc, track.getIsrc());
        assertEquals(filePath, track.getFilePath());
        assertEquals(fileSize, track.getFileSize());
        assertEquals(mockRelease, track.getRelease());
        assertEquals(mockAudioFile, track.getAudioFile());
        assertNotNull(track.getCreatedAt());
    }

    @Test
    void testTrackCreationWithNoArgsConstructor() {
        // When
        Track track = new Track();

        // Then
        assertNotNull(track);
        assertNull(track.getTitle());
        assertEquals(0, track.getDuration());
        assertNull(track.getIsrc());
        assertNull(track.getFilePath());
        assertNull(track.getFileSize());
        assertNull(track.getRelease());
        assertNull(track.getAudioFile());
        assertNotNull(track.getCreatedAt());
    }

    @Test
    void testTrackCreationWithAllArgsConstructor() {
        // Given
        Long id = 1L;
        String title = "Test Track";
        int duration = 180;
        String isrc = "US-123-45-67890";
        String filePath = "/path/to/track.mp3";
        Long fileSize = 1024L;
        OffsetDateTime createdAt = OffsetDateTime.now();

        // When
        Track track = new Track(id, mockRelease, title, duration, isrc, filePath, fileSize, mockAudioFile, createdAt);

        // Then
        assertNotNull(track);
        assertEquals(id, track.getId());
        assertEquals(title, track.getTitle());
        assertEquals(duration, track.getDuration());
        assertEquals(isrc, track.getIsrc());
        assertEquals(filePath, track.getFilePath());
        assertEquals(fileSize, track.getFileSize());
        assertEquals(mockRelease, track.getRelease());
        assertEquals(mockAudioFile, track.getAudioFile());
        assertEquals(createdAt, track.getCreatedAt());
    }

    @Test
    void testDefaultCreatedAtValue() {
        // When
        Track track = Track.builder()
                .title("Test Track")
                .duration(180)
                .filePath("/path/to/track.mp3")
                .release(mockRelease)
                .build();

        // Then
        assertNotNull(track.getCreatedAt());
        assertTrue(track.getCreatedAt().isBefore(OffsetDateTime.now().plusSeconds(1)));
    }

    @Test
    void testTrackSettersAndGetters() {
        // Given
        Track track = new Track();

        // When
        track.setTitle("New Title");
        track.setDuration(200);
        track.setIsrc("US-123-45-67891");
        track.setFilePath("/new/path.mp3");
        track.setFileSize(2048L);
        track.setRelease(mockRelease);
        track.setAudioFile(mockAudioFile);
        OffsetDateTime now = OffsetDateTime.now();
        track.setCreatedAt(now);

        // Then
        assertEquals("New Title", track.getTitle());
        assertEquals(200, track.getDuration());
        assertEquals("US-123-45-67891", track.getIsrc());
        assertEquals("/new/path.mp3", track.getFilePath());
        assertEquals(2048L, track.getFileSize());
        assertEquals(mockRelease, track.getRelease());
        assertEquals(mockAudioFile, track.getAudioFile());
        assertEquals(now, track.getCreatedAt());
    }
} 