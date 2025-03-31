package com.resonate.infrastructure.repository;

import com.resonate.domain.media.AudioFile;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class AudioFileRepository implements PanacheRepository<AudioFile> {
    // PanacheRepository provides built-in CRUD operations.
}
