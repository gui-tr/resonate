package com.resonate.infrastructure.repository;

import com.resonate.domain.model.Track;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class TrackRepository implements PanacheRepository<Track> {
    // Add additional domain-specific queries here
}
