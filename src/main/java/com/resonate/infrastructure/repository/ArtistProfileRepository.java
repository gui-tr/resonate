package com.resonate.infrastructure.repository;

import com.resonate.domain.model.ArtistProfile;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

@ApplicationScoped
public class ArtistProfileRepository implements PanacheRepository<ArtistProfile> {

    public ArtistProfile findByUserId(UUID userId) {
        return find("userId", userId).firstResult();
    }

    @Transactional
    public ArtistProfile upsert(ArtistProfile profile) {
        ArtistProfile existing = findByUserId(profile.getUserId());
        if (existing != null) {
            // Update the fields you want to change
            existing.setBiography(profile.getBiography());
            existing.setSocialLinks(profile.getSocialLinks());
            // (Add additional field updates as needed)
            return existing;
        } else {
            persist(profile);
            return profile;
        }
    }
}
