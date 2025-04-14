package com.resonate.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name = "fan_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FanProfile {

    @Id
    @Column(name = "user_id", nullable = false)
    @JsonProperty("userId")
    private UUID userId;

    @Column(name = "subscription_active", nullable = false)
    @JsonProperty("subscriptionActive")
    private boolean subscriptionActive;

    @Column(name = "subscription_start_date")
    @JsonProperty("subscriptionStartDate")
    private OffsetDateTime subscriptionStartDate;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
