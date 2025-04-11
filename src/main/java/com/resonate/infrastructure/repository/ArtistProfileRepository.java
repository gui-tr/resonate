package com.resonate.infrastructure.repository;

import com.resonate.domain.model.ArtistProfile;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class ArtistProfileRepository implements PanacheRepository<ArtistProfile> {
    public ArtistProfile findByUserId(UUID userId) {
        return find("userId", userId).firstResult();
    }
    public void upsert(ArtistProfile profile) {
        // Upsert logic – for simplicity, persist or update.
        if (profile.getId() == null) {
            persist(profile);
        } else {
            getEntityManager().merge(profile);
        }
    }
}
