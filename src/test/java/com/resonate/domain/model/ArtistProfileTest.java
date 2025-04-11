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
                .build();

        assertNotNull(profile);
        assertEquals(userId, profile.getUserId());
        assertEquals(biography, profile.getBiography());
    }

    @Test
    void testArtistProfileCreationWithNoArgsConstructor() {
        ArtistProfile profile = new ArtistProfile();

        assertNotNull(profile);
        assertNull(profile.getUserId());
        assertNull(profile.getBiography());
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

        assertEquals(newUserId, profile.getUserId());
        assertEquals(newBiography, profile.getBiography());

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
        ArtistProfile profile = builder.build();

        assertNotNull(profile);
        assertEquals(userId, profile.getUserId());
        assertEquals(biography, profile.getBiography());

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