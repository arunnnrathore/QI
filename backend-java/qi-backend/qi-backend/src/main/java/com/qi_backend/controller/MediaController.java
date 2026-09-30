package com.qi_backend.controller;

import com.qi_backend.dto.MediaFileResponse;
import com.qi_backend.entity.MediaFile;
import com.qi_backend.service.MediaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class MediaController {

    private final MediaService mediaService;

    /**
     * Upload a file (multipart/form-data).
     * Returns the file metadata (without binary data).
     */
    @PostMapping("/upload")
    public ResponseEntity<MediaFileResponse> uploadFile(
            @RequestParam("file") MultipartFile file,
            Authentication authentication) {

        String uploaderEmail = authentication.getName();
        MediaFileResponse response = mediaService.uploadFile(uploaderEmail, file);
        return ResponseEntity.ok(response);
    }

    /**
     * Download a file by its ID.
     * Streams the binary data with the correct Content-Type and Content-Disposition headers.
     */
    @GetMapping("/download/{fileId}")
    public ResponseEntity<byte[]> downloadFile(@PathVariable Long fileId) {

        MediaFile mediaFile = mediaService.getFile(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(mediaFile.getContentType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + mediaFile.getOriginalFilename() + "\"")
                .body(mediaFile.getData());
    }

    /**
     * List all files uploaded by the authenticated user.
     */
    @GetMapping("/my-files")
    public ResponseEntity<List<MediaFileResponse>> getMyFiles(Authentication authentication) {

        String uploaderEmail = authentication.getName();
        List<MediaFileResponse> files = mediaService.getMyFiles(uploaderEmail);
        return ResponseEntity.ok(files);
    }
}
