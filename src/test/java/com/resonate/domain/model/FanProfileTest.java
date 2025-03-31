package com.resonate.domain.model;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class FanProfileTest {

    @Test
    void testFanProfileCreationWithBuilder() {
        UUID userId = UUID.randomUUID();
        boolean subscriptionActive = true;
        OffsetDateTime subscriptionStartDate = OffsetDateTime.now();
        OffsetDateTime createdAt = OffsetDateTime.now();

        FanProfile profile = FanProfile.builder()
                .userId(userId)
                .subscriptionActive(subscriptionActive)
                .subscriptionStartDate(subscriptionStartDate)
                .createdAt(createdAt)
                .build();

        assertNotNull(profile);
        assertEquals(userId, profile.getUserId());
        assertTrue(profile.isSubscriptionActive());
        assertEquals(subscriptionStartDate, profile.getSubscriptionStartDate());
        assertEquals(createdAt, profile.getCreatedAt());
    }

    @Test
    void testFanProfileCreationWithNoArgsConstructor() {
        FanProfile profile = new FanProfile();

        assertNotNull(profile);
        assertNull(profile.getUserId());
        assertFalse(profile.isSubscriptionActive());
        assertNull(profile.getSubscriptionStartDate());
        assertNotNull(profile.getCreatedAt());
    }

    @Test
    void testFanProfileCreationWithAllArgsConstructor() {
        UUID userId = UUID.randomUUID();
        boolean subscriptionActive = true;
        OffsetDateTime subscriptionStartDate = OffsetDateTime.now();
        OffsetDateTime createdAt = OffsetDateTime.now();

        FanProfile profile = new FanProfile(userId, subscriptionActive, subscriptionStartDate, createdAt);

        assertNotNull(profile);
        assertEquals(userId, profile.getUserId());
        assertTrue(profile.isSubscriptionActive());
        assertEquals(subscriptionStartDate, profile.getSubscriptionStartDate());
        assertEquals(createdAt, profile.getCreatedAt());
    }

    @Test
    void testDefaultCreatedAtValue() {
        FanProfile profile = FanProfile.builder()
                .userId(UUID.randomUUID())
                .subscriptionActive(true)
                .build();

        assertNotNull(profile.getCreatedAt());
        assertTrue(profile.getCreatedAt().isBefore(OffsetDateTime.now().plusSeconds(1)));
    }

    @Test
    void testFanProfileSettersAndGetters() {
        FanProfile profile = new FanProfile();
        UUID newUserId = UUID.randomUUID();
        boolean newSubscriptionActive = true;
        OffsetDateTime newSubscriptionStartDate = OffsetDateTime.now();
        OffsetDateTime newCreatedAt = OffsetDateTime.now();

        profile.setUserId(newUserId);
        profile.setSubscriptionActive(newSubscriptionActive);
        profile.setSubscriptionStartDate(newSubscriptionStartDate);
        profile.setCreatedAt(newCreatedAt);

        assertEquals(newUserId, profile.getUserId());
        assertTrue(profile.isSubscriptionActive());
        assertEquals(newSubscriptionStartDate, profile.getSubscriptionStartDate());
        assertEquals(newCreatedAt, profile.getCreatedAt());
    }

    @Test
    void testFanProfileBuilderMethods() {
        UUID userId = UUID.randomUUID();
        boolean subscriptionActive = true;
        OffsetDateTime subscriptionStartDate = OffsetDateTime.now();
        OffsetDateTime createdAt = OffsetDateTime.now();

        FanProfile.FanProfileBuilder builder = FanProfile.builder();
        builder.userId(userId);
        builder.subscriptionActive(subscriptionActive);
        builder.subscriptionStartDate(subscriptionStartDate);
        builder.createdAt(createdAt);
        FanProfile profile = builder.build();

        assertNotNull(profile);
        assertEquals(userId, profile.getUserId());
        assertTrue(profile.isSubscriptionActive());
        assertEquals(subscriptionStartDate, profile.getSubscriptionStartDate());
        assertEquals(createdAt, profile.getCreatedAt());
    }

    @Test
    void testFanProfileBuilderToString() {
        FanProfile.FanProfileBuilder builder = FanProfile.builder()
                .userId(UUID.randomUUID())
                .subscriptionActive(true);

        String builderString = builder.toString();

        assertNotNull(builderString);
        assertTrue(builderString.contains("FanProfileBuilder"));
    }
} 