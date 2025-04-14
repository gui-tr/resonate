package com.resonate.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class CreateTrackRequest {
    private String title;
    private Integer duration;
    private String isrc;
    
    @JsonProperty("filePath")
    private String filePath;
    
    @JsonProperty("fileSize")
    private Long fileSize;
    
    @JsonProperty("audioFileId")
    private Long audioFileId;
} 