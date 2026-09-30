package com.qi_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MediaFileResponse {

    private Long id;
    private String originalFilename;
    private String contentType;
    private Long fileSize;
    private Long uploaderId;
    private String uploaderUsername;
    private LocalDateTime uploadedAt;
    private String downloadUrl;
}
