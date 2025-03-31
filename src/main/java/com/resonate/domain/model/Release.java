package com.resonate.domain.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
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
public class Release {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The artist who owns this release
    @Column(name = "artist_id", nullable = false)
    private UUID artistId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "upc")
    private String upc; // Nullable official UPC

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    @Builder.Default
    @OneToMany(mappedBy = "release", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference // Prevents infinite recursion during JSON serialization
    private List<Track> tracks = new ArrayList<>();
}
