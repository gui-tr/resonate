package com.resonate.infrastructure.repository;

import com.resonate.domain.model.Release;
import com.resonate.domain.model.Track;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import java.util.List;

@ApplicationScoped
public class ReleaseRepository implements PanacheRepository<Release> {
    
    @Inject
    TrackRepository trackRepository;
    
    // Add additional domain-specific queries here

    public List<Track> findTracksByReleaseId(Long releaseId) {
        return trackRepository.find("release.id", releaseId).list();
    }
}
