package com.qi_backend.repository;

import com.qi_backend.entity.ChatMessage;
import com.qi_backend.entity.User;
import com.qi_backend.enums.MessageStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("SELECT m FROM ChatMessage m " +
           "JOIN FETCH m.sender " +
           "JOIN FETCH m.receiver " +
           "WHERE (m.sender = :user1 AND m.receiver = :user2) " +
           "   OR (m.sender = :user2 AND m.receiver = :user1) " +
           "ORDER BY m.timestamp ASC")
    List<ChatMessage> findChatHistory(@Param("user1") User user1, @Param("user2") User user2);

    long countByReceiverAndSenderAndStatus(User receiver, User sender, MessageStatus status);

    @Modifying
    @Query("UPDATE ChatMessage m SET m.status = 'READ' " +
           "WHERE m.receiver = :receiver AND m.sender = :sender AND m.status != 'READ'")
    void markMessagesAsRead(@Param("receiver") User receiver, @Param("sender") User sender);
}
