package com.qi_backend.dto;

import com.qi_backend.enums.FriendRequestStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendRequestResponse {

    private Long requestId;

    private Long senderId;

    private String senderUsername;

    private String senderName;

    private FriendRequestStatus status;

    private LocalDateTime createdAt;
}