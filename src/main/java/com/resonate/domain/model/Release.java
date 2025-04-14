package com.resonate.domain.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.*;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "releases")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Release {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "artist_id", nullable = false)
    @JsonProperty("artistId")
    private UUID artistId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "release_date", nullable = false)
    @JsonProperty("releaseDate")
    private LocalDate releaseDate;

    @Column(name = "upc")
    private String upc;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "release", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    @JsonDeserialize(contentAs = Track.class)
    private List<Track> tracks = new ArrayList<>();
}
