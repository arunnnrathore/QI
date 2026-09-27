package com.qi_backend.controller;

import com.qi_backend.dto.AiChatRequest;
import com.qi_backend.dto.AiChatResponse;
import com.qi_backend.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiService aiService;

    @PostMapping("/chat")
    public AiChatResponse chat(
            @RequestBody AiChatRequest request,
            Authentication authentication) {

        String userEmail = authentication.getName();
        return aiService.processChat(userEmail, request);
    }
}
