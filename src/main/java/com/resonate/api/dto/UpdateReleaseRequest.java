package com.resonate.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateReleaseRequest {
    private String title;
    
    @JsonProperty("releaseDate")
    private LocalDate releaseDate;
    
    private String upc;
} 