package com.qi_backend.dto;

import com.qi_backend.enums.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryResponse {

    private Long friendId;
    private String friendUsername;
    private String friendFirstName;
    private String friendLastName;
    private String friendProfilePicture;
    private Boolean friendOnline;
    private LocalDateTime friendLastSeen;

    // Last message details
    private Long lastMessageId;
    private String lastMessageContent;
    private Long lastMessageSenderId;
    private LocalDateTime lastMessageTimestamp;
    private MessageStatus lastMessageStatus;

    // Unread count sent by this friend to the current user
    private long unreadCount;
}
