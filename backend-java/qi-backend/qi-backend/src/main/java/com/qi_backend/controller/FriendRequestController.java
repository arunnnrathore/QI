package com.qi_backend.controller;

import com.qi_backend.service.FriendRequestService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import com.qi_backend.dto.FriendRequestResponse;
import java.util.List;
import com.qi_backend.dto.FriendResponse;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
public class FriendRequestController {

    private final FriendRequestService friendRequestService;

    @PostMapping("/request")
    public String sendFriendRequest
            (@RequestParam Long senderId, @RequestParam Long receiverId)
    {

        return friendRequestService.sendFriendRequest(senderId, receiverId);
    }

    @GetMapping("/pending")
    public List<FriendRequestResponse> getPendingRequests(
            @RequestParam Long receiverId) {

        return friendRequestService.getPendingRequests(receiverId);
    }
//    @PostMapping("/accept")
//    public String acceptFriendRequest(
//            @RequestParam Long requesterId,
//            @RequestParam Long receiverId) {
//
//        return friendRequestService.acceptFriendRequest(requesterId, receiverId);
//    }
    @PostMapping("/accept")
    public String acceptFriendRequest(
            @RequestParam Long requestId,
            @RequestParam Long receiverId) {

        System.out.println("===== ACCEPT API HIT =====");
        System.out.println("Request ID: " + requestId);
        System.out.println("Receiver ID: " + receiverId);

        return friendRequestService.acceptFriendRequest(requestId, receiverId);
    }

    @PostMapping("/reject")
    public String rejectFriendRequest(
            @RequestParam Long requestId,
            @RequestParam Long receiverId) {

        System.out.println("===== REJECT API HIT =====");
        System.out.println("Request ID: " + requestId);
        System.out.println("Receiver ID: " + receiverId);

        return friendRequestService.rejectFriendRequest(requestId, receiverId);
    }

    @PostMapping("/test")
    public String test() {
        System.out.println("TEST API HIT");
        return "OK";
    }
    @GetMapping("/list")
    public List<FriendResponse> getFriends(@RequestParam Long userId)
    {
        return friendRequestService.getFriends(userId);
    }
}