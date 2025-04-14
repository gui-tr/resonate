package com.resonate.domain.model;

import com.resonate.domain.media.AudioFile;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "release_id", nullable = false)
    @JsonBackReference
    private Release release;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "duration", nullable = false)
    private int duration;

    @Column(name = "isrc")
    private String isrc;

    @Column(name = "file_path", nullable = false)
    @JsonProperty("filePath")
    private String filePath;

    @Column(name = "file_size")
    @JsonProperty("fileSize")
    private Long fileSize;

    @OneToOne
    @JoinColumn(name = "audio_file_id")
    @JsonProperty("audioFileId")
    private AudioFile audioFile;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
