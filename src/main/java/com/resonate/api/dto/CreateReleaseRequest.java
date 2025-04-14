package com.resonate.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;
import java.util.UUID;

@Data
public class CreateReleaseRequest {
    @JsonProperty("artistId")
    private UUID artistId;
    
    private String title;
    
    @JsonProperty("releaseDate")
    private LocalDate releaseDate;
    
    private String upc;
} 