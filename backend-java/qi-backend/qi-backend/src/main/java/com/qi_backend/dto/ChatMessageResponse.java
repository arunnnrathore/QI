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
public class ChatMessageResponse {

    private Long id;
    private Long senderId;
    private String senderUsername;
    private String senderName;
    private Long receiverId;
    private String receiverUsername;
    private String receiverName;
    private String content;
    private MessageStatus status;
    private LocalDateTime timestamp;

    // Attachment fields (null if no attachment)
    private Long attachmentId;
    private String attachmentFilename;
    private String attachmentContentType;
    private String attachmentDownloadUrl;
}
