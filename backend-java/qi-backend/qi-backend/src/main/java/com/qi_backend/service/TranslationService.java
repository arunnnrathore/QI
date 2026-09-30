package com.qi_backend.service;

import com.qi_backend.dto.TranslationRequest;
import com.qi_backend.dto.TranslationResponse;
import com.qi_backend.entity.ChatMessage;
import com.qi_backend.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TranslationService {

    private final ChatMessageRepository chatMessageRepository;
    private final com.qi_backend.repository.UserRepository userRepository;

    public TranslationResponse translateText(TranslationRequest request) {
        if (request.getText() == null || request.getText().trim().isEmpty()) {
            throw new RuntimeException("Text to translate cannot be empty");
        }
        if (request.getTargetLanguage() == null || request.getTargetLanguage().trim().isEmpty()) {
            throw new RuntimeException("Target language must be specified");
        }

        String translated = mockTranslate(request.getText(), request.getTargetLanguage());

        return TranslationResponse.builder()
                .originalText(request.getText())
                .translatedText(translated)
                .targetLanguage(request.getTargetLanguage())
                .build();
    }

    public TranslationResponse translateMessage(Long messageId, String targetLanguage, String userEmail) {
        ChatMessage message = chatMessageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found with id: " + messageId));
        
        com.qi_backend.entity.User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!message.getSender().getId().equals(currentUser.getId()) && 
            !message.getReceiver().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You do not have permission to view this message");
        }
        
        TranslationRequest request = new TranslationRequest(message.getContent(), targetLanguage);
        return translateText(request);
    }

    public TranslationResponse translateAudio(org.springframework.web.multipart.MultipartFile audioFile, String targetLanguage) {
        if (audioFile == null || audioFile.isEmpty()) {
            throw new RuntimeException("Audio file cannot be empty");
        }
        if (targetLanguage == null || targetLanguage.trim().isEmpty()) {
            throw new RuntimeException("Target language must be specified");
        }

        String mockTranscription = "[Audio transcription of " + audioFile.getOriginalFilename() + "]";
        String translated = mockTranslate(mockTranscription, targetLanguage);

        return TranslationResponse.builder()
                .originalText(mockTranscription)
                .translatedText(translated)
                .targetLanguage(targetLanguage)
                .build();
    }

    // A simple mock translation engine to get started without an API key
    private String mockTranslate(String text, String targetLang) {
        String lowerLang = targetLang.toLowerCase();
        String suffix = "";
        
        switch (lowerLang) {
            case "es":
            case "spanish":
                suffix = " (traducido al español)";
                break;
            case "fr":
            case "french":
                suffix = " (traduit en français)";
                break;
            case "de":
            case "german":
                suffix = " (auf Deutsch übersetzt)";
                break;
            case "hi":
            case "hindi":
                suffix = " (हिंदी में अनुवादित)";
                break;
            default:
                suffix = " [Translated to " + targetLang + "]";
                break;
        }

        return text + suffix;
    }
}
