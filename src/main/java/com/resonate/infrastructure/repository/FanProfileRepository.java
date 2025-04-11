package com.resonate.infrastructure.repository;

import com.resonate.domain.model.FanProfile;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.UUID;

@ApplicationScoped
public class FanProfileRepository implements PanacheRepository<FanProfile> {
    public FanProfile findByUserId(UUID userId) {
        return find("userId", userId).firstResult();
    }
    public void upsert(FanProfile profile) {
        if (profile.getId() == null) {
            persist(profile);
        } else {
            getEntityManager().merge(profile);
        }
    }
}
