package com.qi_backend.controller;

import com.qi_backend.dto.ChatMessageResponse;
import com.qi_backend.dto.ConversationSummaryResponse;
import com.qi_backend.dto.SendMessageRequest;
import com.qi_backend.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // HTTP endpoint for fetching recent conversations list with last message snippet & unread count
    @GetMapping("/conversations")
    public List<ConversationSummaryResponse> getRecentConversations(Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return chatService.getRecentConversations(currentUserEmail);
    }

    // HTTP endpoint for sending messages (Authenticated via JWT)
    @PostMapping("/send")
    public ChatMessageResponse sendMessage(
            @RequestBody SendMessageRequest request,
            Authentication authentication) {

        String senderEmail = authentication.getName();
        return chatService.sendMessage(senderEmail, request.getReceiverId(), request.getContent());
    }

    // HTTP endpoint for fetching chat history between current user and friend
    @GetMapping("/history")
    public List<ChatMessageResponse> getChatHistory(
            @RequestParam Long friendId,
            Authentication authentication) {

        String currentUserEmail = authentication.getName();
        return chatService.getChatHistory(currentUserEmail, friendId);
    }

    // WebSocket / STOMP endpoint: clients can also publish to /app/chat.send
    @MessageMapping("/chat.send")
    public ChatMessageResponse handleWebSocketMessage(
            @Payload SendMessageRequest request,
            Principal principal) {

        String senderEmail = principal.getName();
        return chatService.sendMessage(senderEmail, request.getReceiverId(), request.getContent());
    }
}
