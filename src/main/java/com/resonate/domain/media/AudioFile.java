package com.resonate.domain.media;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import jakarta.persistence.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "audio_files")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AudioFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // The file identifier returned from Backblaze B2 (or file name/path)
    @Column(name = "file_identifier", nullable = false)
    @JsonProperty("fileIdentifier")
    private String fileIdentifier;

    // URL of the uploaded file (could be a signed URL for streaming)
    @Column(name = "file_url", nullable = false)
    @JsonProperty("fileUrl")
    private String fileUrl;

    // File size in bytes
    @Column(name = "file_size")
    @JsonProperty("fileSize")
    private Long fileSize;

    // Additional metadata, such as checksum, content type, etc.
    @Column(name = "checksum")
    private String checksum;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    @JsonProperty("createdAt")
    private OffsetDateTime createdAt = OffsetDateTime.now();
}
