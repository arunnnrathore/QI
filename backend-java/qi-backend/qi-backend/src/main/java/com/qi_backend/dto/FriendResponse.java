package com.qi_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendResponse {

    private Long id;

    private String username;

    private String firstName;

    private String lastName;

    private String profilePicture;
}