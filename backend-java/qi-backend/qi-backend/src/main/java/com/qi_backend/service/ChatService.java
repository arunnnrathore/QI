package com.qi_backend.service;

import com.qi_backend.dto.ChatMessageResponse;
import com.qi_backend.dto.ConversationSummaryResponse;
import com.qi_backend.entity.ChatMessage;
import com.qi_backend.entity.Friend;
import com.qi_backend.entity.MediaFile;
import com.qi_backend.entity.User;
import com.qi_backend.enums.MessageStatus;
import com.qi_backend.repository.ChatMessageRepository;
import com.qi_backend.repository.FriendRepository;
import com.qi_backend.repository.MediaFileRepository;
import com.qi_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;
    private final MediaFileRepository mediaFileRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public ChatMessageResponse sendMessage(String senderEmail, Long receiverId, String content, Long attachmentId) {

        if ((content == null || content.trim().isEmpty()) && attachmentId == null) {
            throw new RuntimeException("Message must have content or an attachment");
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

        // Look up optional attachment
        MediaFile attachment = null;
        if (attachmentId != null) {
            attachment = mediaFileRepository.findById(attachmentId)
                    .orElseThrow(() -> new RuntimeException("Attachment not found with id: " + attachmentId));
        }

        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .receiver(receiver)
                .content(content != null ? content.trim() : "")
                .attachment(attachment)
                .status(MessageStatus.SENT)
                .timestamp(LocalDateTime.now())
                .build();

        ChatMessage savedMessage = chatMessageRepository.save(message);

        ChatMessageResponse response = mapToResponse(savedMessage);

        // Deliver only to the authenticated users' private STOMP queues.
        messagingTemplate.convertAndSendToUser(receiver.getEmail(), "/queue/messages", response);
        messagingTemplate.convertAndSendToUser(sender.getEmail(), "/queue/messages", response);

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

    @Transactional(readOnly = true)
    public List<ConversationSummaryResponse> getRecentConversations(String currentUserEmail) {
        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        List<Friend> friendsList = friendRepository.findByUserWithFriend(currentUser);

        List<ConversationSummaryResponse> conversations = new ArrayList<>();

        for (Friend friendRelation : friendsList) {
            User friend = friendRelation.getFriend();

            // Find the most recent message between currentUser and friend
            List<ChatMessage> latestMessages = chatMessageRepository.findLatestMessageBetweenUsers(
                    currentUser,
                    friend,
                    PageRequest.of(0, 1)
            );

            ChatMessage lastMessage = latestMessages.isEmpty() ? null : latestMessages.get(0);

            // Count unread messages sent by friend to currentUser
            long unreadCount = chatMessageRepository.countByReceiverAndSenderAndStatus(
                    currentUser,
                    friend,
                    MessageStatus.SENT
            );

            ConversationSummaryResponse summary = ConversationSummaryResponse.builder()
                    .friendId(friend.getId())
                    .friendUsername(friend.getUsername())
                    .friendFirstName(friend.getFirstName())
                    .friendLastName(friend.getLastName())
                    .friendProfilePicture(friend.getProfilePicture())
                    .friendOnline(friend.getOnline())
                    .friendLastSeen(friend.getLastSeen())
                    .lastMessageId(lastMessage != null ? lastMessage.getId() : null)
                    .lastMessageContent(lastMessage != null ? lastMessage.getContent() : null)
                    .lastMessageSenderId(lastMessage != null ? lastMessage.getSender().getId() : null)
                    .lastMessageTimestamp(lastMessage != null ? lastMessage.getTimestamp() : null)
                    .lastMessageStatus(lastMessage != null ? lastMessage.getStatus() : null)
                    .unreadCount(unreadCount)
                    .build();

            conversations.add(summary);
        }

        // Sort: conversations with the most recent messages first.
        // Conversations without any messages yet are sorted by friend username or placed at the end.
        conversations.sort((c1, c2) -> {
            if (c1.getLastMessageTimestamp() != null && c2.getLastMessageTimestamp() != null) {
                return c2.getLastMessageTimestamp().compareTo(c1.getLastMessageTimestamp());
            } else if (c1.getLastMessageTimestamp() != null) {
                return -1;
            } else if (c2.getLastMessageTimestamp() != null) {
                return 1;
            } else {
                return c1.getFriendUsername().compareToIgnoreCase(c2.getFriendUsername());
            }
        });

        return conversations;
    }

    private ChatMessageResponse mapToResponse(ChatMessage message) {
        ChatMessageResponse.ChatMessageResponseBuilder builder = ChatMessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .senderName(message.getSender().getFirstName() + " " + (message.getSender().getLastName() != null ? message.getSender().getLastName() : ""))
                .receiverId(message.getReceiver().getId())
                .receiverUsername(message.getReceiver().getUsername())
                .receiverName(message.getReceiver().getFirstName() + " " + (message.getReceiver().getLastName() != null ? message.getReceiver().getLastName() : ""))
                .content(message.getContent())
                .status(message.getStatus())
                .timestamp(message.getTimestamp());

        if (message.getAttachment() != null) {
            MediaFile attachment = message.getAttachment();
            builder.attachmentId(attachment.getId())
                    .attachmentFilename(attachment.getOriginalFilename())
                    .attachmentContentType(attachment.getContentType())
                    .attachmentDownloadUrl("/api/files/download/" + attachment.getId());
        }

        return builder.build();
    }
}
