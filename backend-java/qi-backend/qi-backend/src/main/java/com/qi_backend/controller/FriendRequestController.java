package com.qi_backend.controller;

import com.qi_backend.dto.FriendRequestResponse;
import com.qi_backend.dto.FriendResponse;
import com.qi_backend.service.FriendRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import java.util.List;
import org.springframework.security.core.Authentication;


@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    // Send request to another user
    @PostMapping("/request")
    public String sendFriendRequest(
            @RequestParam Long receiverId,
            Authentication authentication) {

        String email = authentication.getName();

        return friendRequestService.sendFriendRequest(
                email,
                receiverId
        );
    }


    // Get requests received by the logged-in user
    @GetMapping("/pending")
    public List<FriendRequestResponse> getPendingRequests(
            Authentication authentication) {

        return friendRequestService.getPendingRequests(authentication.getName());
    }

    //Accept request as the logged in user
    @PostMapping("/accept")
    public String acceptFriendRequest(
            @RequestParam Long requestId,
            Authentication authentication) {

        String email = authentication.getName();

        return friendRequestService.acceptFriendRequest(requestId, email);
    }

    // Reject request as the logged-in user
    @PostMapping("/reject")
    public String rejectFriendRequest(
            @RequestParam Long requestId) {

        return friendRequestService.rejectFriendRequest(requestId);
    }

    // Remove a friend from the logged-in user's account
    @DeleteMapping("/unfriend")
    public String unfriend(
            @RequestParam Long friendId) {

        return friendRequestService.unfriend(friendId);
    }

    // Get friends of the logged-in user
    @GetMapping("/list")
    public List<FriendResponse> getFriends() {

        return friendRequestService.getFriends();
    }

    @PostMapping("/test")
    public String test() {
        return "OK";
    }
}