package com.resonate.infrastructure.repository;

import com.resonate.domain.model.Release;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReleaseRepository implements PanacheRepository<Release> {
    // Add additional domain-specific queries here
}
