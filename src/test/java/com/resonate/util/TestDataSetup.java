package com.resonate.util;

import com.resonate.domain.media.AudioFile;
import com.resonate.domain.model.ArtistProfile;
import com.resonate.domain.model.FanProfile;
import com.resonate.domain.model.Release;
import com.resonate.domain.model.Track;
import com.resonate.infrastructure.repository.ArtistProfileRepository;
import com.resonate.infrastructure.repository.AudioFileRepository;
import com.resonate.infrastructure.repository.FanProfileRepository;
import com.resonate.infrastructure.repository.ReleaseRepository;
import com.resonate.infrastructure.repository.TrackRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;


@ApplicationScoped
public class TestDataSetup {

    @Inject
    EntityManager entityManager;

    @Inject
    ArtistProfileRepository artistProfileRepository;

    @Inject
    FanProfileRepository fanProfileRepository;

    @Inject
    ReleaseRepository releaseRepository;

    @Inject
    TrackRepository trackRepository;

    @Inject
    AudioFileRepository audioFileRepository;

    /**
     * Create an artist profile with a unique ID
     */
    @Transactional
    public ArtistProfile createArtistProfile() {
        UUID uniqueId = UUID.randomUUID();
        return createArtistProfile(uniqueId);
    }

    /**
     * Create an artist profile with a specific ID
     */
    @Transactional
    public ArtistProfile createArtistProfile(UUID userId) {
        // Before creating, check if this ID matches one of our fixed test users
        if (userId.equals(TestUtil.ARTIST_UUID)) {
            // Return the existing artist profile from the test DB
            return artistProfileRepository.findByUserId(userId);
        }

        ArtistProfile profile = ArtistProfile.builder()
                .userId(userId)
                .biography("Test Artist Biography")
                .socialLinks("{\"twitter\":\"@testartist\"}")
                .build();

        entityManager.persist(profile);
        entityManager.flush();
        return profile;
    }

    /**
     * Create a fan profile with a unique ID
     */
    @Transactional
    public FanProfile createFanProfile() {
        UUID uniqueId = UUID.randomUUID();
        return createFanProfile(uniqueId);
    }

    /**
     * Create a fan profile with a specific ID
     */
    @Transactional
    public FanProfile createFanProfile(UUID userId) {
        // Before creating, check if this ID matches one of our fixed test users
        if (userId.equals(TestUtil.FAN_UUID)) {
            // Return the existing fan profile from the test DB
            return fanProfileRepository.findByUserId(userId);
        }

        FanProfile profile = FanProfile.builder()
                .userId(userId)
                .subscriptionActive(true)
                .subscriptionStartDate(OffsetDateTime.now())
                .build();

        entityManager.persist(profile);
        entityManager.flush();
        return profile;
    }

    @Transactional
    public Release createRelease(UUID artistId, String title) {
        Release release = Release.builder()
                .artistId(artistId)
                .title(title)
                .releaseDate(LocalDate.now())
                .upc("123456789012")
                .build();

        entityManager.persist(release);
        entityManager.flush();
        return release;
    }

    @Transactional
    public Track createTrack(Release release, String title) {
        Track track = Track.builder()
                .release(release)
                .title(title)
                .duration(180)
                .isrc("TEST123456789")
                .filePath("/test/path.mp3")
                .fileSize(1024L)
                .build();

        entityManager.persist(track);
        entityManager.flush();
        return track;
    }

    @Transactional
    public AudioFile createAudioFile(String identifier) {
        AudioFile audioFile = AudioFile.builder()
                .fileIdentifier(identifier)
                .fileUrl("https://test-bucket.backblaze.com/" + identifier)
                .fileSize(1024L)
                .checksum("test-checksum-" + identifier)
                .build();

        entityManager.persist(audioFile);
        entityManager.flush();
        return audioFile;
    }

    @Transactional
    public void linkTrackToAudioFile(Track track, AudioFile audioFile) {
        track.setAudioFile(audioFile);
        entityManager.merge(track);
        entityManager.flush();
    }

    /**
     * Clean up all test data while preserving fixed test entities
     */
    @Transactional
    public void cleanupTestData() {
        // Delete all tracks
        trackRepository.deleteAll();

        // Delete all releases except those for fixed test artists
        releaseRepository.delete("artistId != ?1 and artistId != ?2",
                TestUtil.ARTIST_UUID, TestUtil.FAN_UUID);

        // Delete any audio files created during tests
        audioFileRepository.deleteAll();

        // Delete any artist profiles except our fixed test ones
        artistProfileRepository.delete("userId != ?1 and userId != ?2",
                TestUtil.ARTIST_UUID, TestUtil.FAN_UUID);

        // Delete any fan profiles except our fixed test ones
        fanProfileRepository.delete("userId != ?1 and userId != ?2",
                TestUtil.ARTIST_UUID, TestUtil.FAN_UUID);

        entityManager.flush();
    }

    public void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
