package com.resonate.infrastructure.repository;

import com.resonate.domain.model.FanProfile;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.util.UUID;

@ApplicationScoped
public class FanProfileRepository implements PanacheRepository<FanProfile> {

    public FanProfile findByUserId(UUID userId) {
        return find("userId", userId).firstResult();
    }

    @Transactional
    public FanProfile upsert(FanProfile profile) {
        FanProfile existing = findByUserId(profile.getUserId());
        if (existing != null) {
            existing.setSubscriptionActive(profile.isSubscriptionActive());
            existing.setSubscriptionStartDate(profile.getSubscriptionStartDate());
            return existing;
        } else {
            persist(profile);
            return profile;
        }
    }


}
