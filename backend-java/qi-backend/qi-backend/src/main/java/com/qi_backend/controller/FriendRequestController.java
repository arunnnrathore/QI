package com.qi_backend.controller;

import com.qi_backend.service.FriendRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    @PostMapping("/request")
    public String sendFriendRequest(
            @RequestParam Long senderId,
            @RequestParam Long receiverId) {

        return friendRequestService.sendFriendRequest(senderId, receiverId);
    }
}