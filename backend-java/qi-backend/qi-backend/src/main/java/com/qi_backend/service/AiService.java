package com.qi_backend.service;

import com.qi_backend.dto.AiChatRequest;
import com.qi_backend.dto.AiChatResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiService {

    @Value("${ai.provider:builtin}")
    private String aiProvider;

    @Value("${ai.gemini.api-key:}")
    private String geminiApiKey;

    public AiChatResponse processChat(String userEmail, AiChatRequest request) {
        String prompt = request.getPrompt();
        if (prompt == null || prompt.trim().isEmpty()) {
            throw new RuntimeException("Prompt cannot be empty");
        }

        String cleanedPrompt = prompt.trim();

        // If an external key is configured in the future, we can delegate to external API here.
        // For now, generate high-quality intelligent responses using the built-in QI Intelligence Engine:
        String reply = generateBuiltInResponse(cleanedPrompt, userEmail);

        return AiChatResponse.builder()
                .response(reply)
                .model("qi-intelligence-v1 (builtin)")
                .timestamp(LocalDateTime.now())
                .build();
    }

    private String generateBuiltInResponse(String prompt, String userEmail) {
        String lower = prompt.toLowerCase(Locale.ROOT);

        if (lower.contains("hello") || lower.contains("hi") || lower.contains("hey")) {
            return "Hello! I am QI Assistant, your personal Quick Intelligence AI companion. How can I assist you with your messages, tasks, or questions today?";
        }

        if (lower.contains("who are you") || lower.contains("what is qi") || lower.contains("what are you")) {
            return "I am the QI Assistant integrated directly into the QI (Quick Intelligence) platform. I can help answer questions, summarize messages, assist with language translation, and help you navigate your conversations!";
        }

        if (lower.contains("features") || lower.contains("what can you do") || lower.contains("help")) {
            return "Here are some of the key features of the QI platform:\n" +
                    "• Real-time chat & WebSocket messaging\n" +
                    "• Friend requests & user discovery\n" +
                    "• User profiles & status tracking\n" +
                    "• Smart Assistant integration\n" +
                    "• Large-file sharing and language translation (coming soon!)";
        }

        if (lower.contains("time") || lower.contains("date")) {
            return "The current server time is " + LocalDateTime.now() + ".";
        }

        if (lower.contains("translate")) {
            return "Translation engine preview: You requested translation assistance. Full multi-language neural translation pipeline will be connected with language models!";
        }

        // Generic intelligent fallback
        return "I received your query: \"" + prompt + "\".\n\n" +
                "As QI Assistant, I'm ready to help you with communication, knowledge retrieval, and productivity. Let me know what specific task you'd like to perform!";
    }
}
