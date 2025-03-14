package com.resonate.domain.model;

import lombok.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;


@Entity
@Table(name = "tracks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Many-to-one relationship with the Release aggregate
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "release_id", nullable = false)
    private Release release;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "duration", nullable = false)
    private int duration; // Duration in seconds

    @Column(name = "isrc")
    private String isrc; // Nullable official ISRC

    @Column(name = "file_path", nullable = false)
    private String filePath; // Reference to audio file in Backblaze B2

    @Column(name = "file_size")
    private Long fileSize; // Optional file size in bytes

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
