package com.resonate.util;

import com.resonate.domain.media.AudioFile;
import com.resonate.domain.model.ArtistProfile;
import com.resonate.domain.model.FanProfile;
import com.resonate.domain.model.Release;
import com.resonate.domain.model.Track;
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


    @Transactional
    public ArtistProfile createArtistProfile(UUID userId) {
        ArtistProfile profile = ArtistProfile.builder()
                .userId(userId)
                .biography("Test Artist Biography")
                .socialLinks("{\"twitter\":\"@testartist\"}")
                .build();

        entityManager.persist(profile);
        entityManager.flush();
        return profile;
    }

    @Transactional
    public FanProfile createFanProfile(UUID userId) {
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

    @Transactional
    public void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
