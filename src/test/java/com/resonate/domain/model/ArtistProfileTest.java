package com.resonate.domain.model;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;
import java.time.OffsetDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class ArtistProfileTest {

    @Test
    void testArtistProfileCreationWithBuilder() {
        UUID userId = UUID.randomUUID();
        String biography = "Test Biography";
        String socialLinks = "https://twitter.com/test";
        OffsetDateTime createdAt = OffsetDateTime.now();

        ArtistProfile profile = ArtistProfile.builder()
                .userId(userId)
                .biography(biography)
                .socialLinks(socialLinks)
                .createdAt(createdAt)
                .build();

        assertNotNull(profile);
        assertEquals(userId, profile.getUserId());
        assertEquals(biography, profile.getBiography());
        assertEquals(socialLinks, profile.getSocialLinks());
        assertEquals(createdAt, profile.getCreatedAt());
    }

    @Test
    void testArtistProfileCreationWithNoArgsConstructor() {
        ArtistProfile profile = new ArtistProfile();

        assertNotNull(profile);
        assertNull(profile.getUserId());
        assertNull(profile.getBiography());
        assertNull(profile.getSocialLinks());
        assertNotNull(profile.getCreatedAt());
    }

    @Test
    void testArtistProfileCreationWithAllArgsConstructor() {
        UUID userId = UUID.randomUUID();
        String biography = "Test Biography";
        String socialLinks = "https://twitter.com/test";
        OffsetDateTime createdAt = OffsetDateTime.now();

        ArtistProfile profile = new ArtistProfile(userId, biography, socialLinks, createdAt);

        assertNotNull(profile);
        assertEquals(userId, profile.getUserId());
        assertEquals(biography, profile.getBiography());
        assertEquals(socialLinks, profile.getSocialLinks());
        assertEquals(createdAt, profile.getCreatedAt());
    }

    @Test
    void testDefaultCreatedAtValue() {
        ArtistProfile profile = ArtistProfile.builder()
                .userId(UUID.randomUUID())
                .biography("Test Biography")
                .build();

        assertNotNull(profile.getCreatedAt());
        assertTrue(profile.getCreatedAt().isBefore(OffsetDateTime.now().plusSeconds(1)));
    }

    @Test
    void testArtistProfileSettersAndGetters() {
        ArtistProfile profile = new ArtistProfile();
        UUID newUserId = UUID.randomUUID();
        String newBiography = "New Biography";
        String newSocialLinks = "https://instagram.com/test";
        OffsetDateTime newCreatedAt = OffsetDateTime.now();

        profile.setUserId(newUserId);
        profile.setBiography(newBiography);
        profile.setSocialLinks(newSocialLinks);
        profile.setCreatedAt(newCreatedAt);

        assertEquals(newUserId, profile.getUserId());
        assertEquals(newBiography, profile.getBiography());
        assertEquals(newSocialLinks, profile.getSocialLinks());
        assertEquals(newCreatedAt, profile.getCreatedAt());
    }

    @Test
    void testArtistProfileBuilderMethods() {
        UUID userId = UUID.randomUUID();
        String biography = "Test Biography";
        String socialLinks = "https://twitter.com/test";
        OffsetDateTime createdAt = OffsetDateTime.now();

        ArtistProfile.ArtistProfileBuilder builder = ArtistProfile.builder();
        builder.userId(userId);
        builder.biography(biography);
        builder.socialLinks(socialLinks);
        builder.createdAt(createdAt);
        ArtistProfile profile = builder.build();

        assertNotNull(profile);
        assertEquals(userId, profile.getUserId());
        assertEquals(biography, profile.getBiography());
        assertEquals(socialLinks, profile.getSocialLinks());
        assertEquals(createdAt, profile.getCreatedAt());
    }

    @Test
    void testArtistProfileBuilderToString() {
        ArtistProfile.ArtistProfileBuilder builder = ArtistProfile.builder()
                .userId(UUID.randomUUID())
                .biography("Test Biography");

        String builderString = builder.toString();

        assertNotNull(builderString);
        assertTrue(builderString.contains("ArtistProfileBuilder"));
    }
} 