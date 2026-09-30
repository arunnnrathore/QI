package com.qi_backend.controller;

import com.qi_backend.dto.TranslationRequest;
import com.qi_backend.dto.TranslationResponse;
import com.qi_backend.service.TranslationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/translation")
@RequiredArgsConstructor
public class TranslationController {

    private final TranslationService translationService;

    @PostMapping("/translate")
    public TranslationResponse translateText(@RequestBody TranslationRequest request) {
        return translationService.translateText(request);
    }

    @GetMapping("/message/{messageId}")
    public TranslationResponse translateMessage(
            @PathVariable Long messageId,
            @RequestParam String targetLanguage,
            org.springframework.security.core.Authentication authentication) {
        String userEmail = authentication.getName();
        return translationService.translateMessage(messageId, targetLanguage, userEmail);
    }

    @PostMapping("/audio")
    public TranslationResponse translateAudio(
            @RequestParam("audio") org.springframework.web.multipart.MultipartFile audioFile,
            @RequestParam("targetLanguage") String targetLanguage) {
        return translationService.translateAudio(audioFile, targetLanguage);
    }
}
