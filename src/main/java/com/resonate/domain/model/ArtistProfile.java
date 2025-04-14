package com.resonate.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;


@Entity
@Table(name = "artist_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ArtistProfile {

    @Id
    @Column(name = "user_id", nullable = false)
    @JsonProperty("userId")
    private UUID userId;

    @Column(name = "biography")
    private String biography;

    @Column(name = "social_links")
    @JsonProperty("socialLinks")
    private String socialLinks;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
