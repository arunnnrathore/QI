package com.qi_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    private Long receiverId;
    private String content;
    private Long attachmentId; // optional: ID of a previously uploaded MediaFile
}
