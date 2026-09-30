package com.qi_backend.service;

import com.qi_backend.dto.MediaFileResponse;
import com.qi_backend.entity.MediaFile;
import com.qi_backend.entity.User;
import com.qi_backend.repository.MediaFileRepository;
import com.qi_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaFileRepository mediaFileRepository;
    private final UserRepository userRepository;

    @Transactional
    public MediaFileResponse uploadFile(String uploaderEmail, MultipartFile file) {

        if (file.isEmpty()) {
            throw new RuntimeException("Cannot upload an empty file");
        }

        User uploader = userRepository.findByEmail(uploaderEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        try {
            MediaFile mediaFile = MediaFile.builder()
                    .uploader(uploader)
                    .originalFilename(file.getOriginalFilename())
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .data(file.getBytes())
                    .build();

            MediaFile saved = mediaFileRepository.save(mediaFile);
            return mapToResponse(saved);

        } catch (IOException e) {
            throw new RuntimeException("Failed to read file data: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public MediaFile getFile(Long fileId) {
        return mediaFileRepository.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));
    }

    @Transactional(readOnly = true)
    public List<MediaFileResponse> getMyFiles(String uploaderEmail) {

        User uploader = userRepository.findByEmail(uploaderEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        return mediaFileRepository.findByUploaderOrderByUploadedAtDesc(uploader)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private MediaFileResponse mapToResponse(MediaFile mediaFile) {
        return MediaFileResponse.builder()
                .id(mediaFile.getId())
                .originalFilename(mediaFile.getOriginalFilename())
                .contentType(mediaFile.getContentType())
                .fileSize(mediaFile.getFileSize())
                .uploaderId(mediaFile.getUploader().getId())
                .uploaderUsername(mediaFile.getUploader().getUsername())
                .uploadedAt(mediaFile.getUploadedAt())
                .downloadUrl("/api/files/download/" + mediaFile.getId())
                .build();
    }
}
