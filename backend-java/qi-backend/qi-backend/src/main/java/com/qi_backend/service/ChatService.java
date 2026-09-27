package com.qi_backend.service;

import com.qi_backend.dto.ChatMessageResponse;
import com.qi_backend.entity.ChatMessage;
import com.qi_backend.entity.User;
import com.qi_backend.enums.MessageStatus;
import com.qi_backend.repository.ChatMessageRepository;
import com.qi_backend.repository.FriendRepository;
import com.qi_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMessageResponse sendMessage(String senderEmail, Long receiverId, String content) {

        if (content == null || content.trim().isEmpty()) {
            throw new RuntimeException("Message content cannot be empty");
        }

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (sender.getId().equals(receiver.getId())) {
            throw new RuntimeException("You cannot send a message to yourself");
        }

        // Verify friendship
        if (!friendRepository.existsByUserAndFriend(sender, receiver)) {
            throw new RuntimeException("You can only message users who are your friends");
        }

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content.trim())
                .status(MessageStatus.SENT)
                .timestamp(LocalDateTime.now())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        ChatMessageResponse response = mapToResponse(savedMessage);

        // Real-time broadcast to WebSocket topics for both users
        messagingTemplate.convertAndSend("/topic/messages/" + receiver.getId(), response);
        messagingTemplate.convertAndSend("/topic/messages/" + sender.getId(), response);

        return response;
    }

    @Transactional
    public List<ChatMessageResponse> getChatHistory(String currentUserEmail, Long friendId) {

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        User friend = userRepository.findById(friendId)
                .orElseThrow(() -> new RuntimeException("Friend not found"));

        // Fetch all messages exchanged between these two users
        List<ChatMessage> history = chatMessageRepository.findChatHistory(currentUser, friend);

        // Mark incoming messages from this friend as READ
        chatMessageRepository.markMessagesAsRead(currentUser, friend);

        return history.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private ChatMessageResponse mapToResponse(ChatMessage message) {
        return ChatMessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .senderName(message.getSender().getFirstName() + " " + (message.getSender().getLastName() != null ? message.getSender().getLastName() : ""))
                .receiverId(message.getReceiver().getId())
                .receiverUsername(message.getReceiver().getUsername())
                .receiverName(message.getReceiver().getFirstName() + " " + (message.getReceiver().getLastName() != null ? message.getReceiver().getLastName() : ""))
                .content(message.getContent())
                .status(message.getStatus())
                .timestamp(message.getTimestamp())
                .build();
    }
}
