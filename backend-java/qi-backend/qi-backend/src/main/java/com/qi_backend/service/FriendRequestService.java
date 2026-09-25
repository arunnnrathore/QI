package com.qi_backend.service;

import com.qi_backend.entity.Friend;
import com.qi_backend.entity.FriendRequest;
import com.qi_backend.dto.FriendRequestResponse;
import com.qi_backend.entity.User;
import com.qi_backend.repository.FriendRequestRepository;
import com.qi_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import com.qi_backend.enums.FriendRequestStatus;
import com.qi_backend.repository.FriendRepository;
import com.qi_backend.dto.FriendResponse;

@Service
@RequiredArgsConstructor
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    public String sendFriendRequest(Long senderId, Long receiverId)
    {
        if (senderId.equals(receiverId))
        {
            return "You cannot send a friend request to yourself.";
        }

        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (friendRequestRepository.findBySenderAndReceiver(sender, receiver).isPresent()) {
            return "Friend request already sent.";
        }

        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .build();

        friendRequestRepository.save(request);

        return "Friend request sent successfully!";
    }
    public List<FriendRequestResponse> getPendingRequests(Long receiverId) {

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return friendRequestRepository.findByReceiverAndStatus(
                        receiver,
                        FriendRequestStatus.PENDING
                ).stream()
                .map(request -> FriendRequestResponse.builder()
                        .requestId(request.getId())
                        .senderId(request.getSender().getId())
                        .senderUsername(request.getSender().getUsername())
                        .senderName(
                                request.getSender().getFirstName()
                                        + " "
                                        + request.getSender().getLastName()
                        )
                        .status(request.getStatus())
                        .createdAt(request.getCreatedAt())
                        .build())
                .toList();
    }
//    public String acceptFriendRequest(Long requestId, Long receiverId) {
//
//        User receiver = userRepository.findById(receiverId)
//                .orElseThrow(() -> new RuntimeException("Receiver not found"));
//
//        FriendRequest request = friendRequestRepository
//                .findByIdAndReceiver(requestId, receiver)
//                .orElseThrow(() -> new RuntimeException("Friend request not found"));
//
//        if (request.getStatus() != FriendRequestStatus.PENDING) {
//            return "This friend request has already been processed.";
//        }
//
//        request.setStatus(FriendRequestStatus.ACCEPTED);
//        friendRequestRepository.save(request);
//
//        // Create friendship (Sender -> Receiver)
//        Friend friendship1 = Friend.builder()
//                .user(request.getSender())
//                .friend(request.getReceiver())
//                .build();
//
//        // Create friendship (Receiver -> Sender)
//        Friend friendship2 = Friend.builder()
//                .user(request.getReceiver())
//                .friend(request.getSender())
//                .build();
//
//        friendRepository.save(friendship1);
//        friendRepository.save(friendship2);
//
//        return "Friend request accepted successfully!";
//    }
        public String acceptFriendRequest(Long requestId, Long receiverId) {
    User receiver = userRepository.findById(receiverId)
            .orElseThrow(() -> new RuntimeException("Receiver not found"));

    FriendRequest request = friendRequestRepository
            .findByIdAndReceiver(requestId, receiver)
            .orElseThrow(() -> new RuntimeException("Friend request not found"));

    if (request.getStatus() != FriendRequestStatus.PENDING) {
        return "This friend request has already been processed.";
    }

    request.setStatus(FriendRequestStatus.ACCEPTED);
    friendRequestRepository.save(request);

    System.out.println("Status updated");

    Friend friendship1 = Friend.builder()
            .user(request.getSender())
            .friend(request.getReceiver())
            .build();

    Friend friendship2 = Friend.builder()
            .user(request.getReceiver())
            .friend(request.getSender())
            .build();

    System.out.println("Friend objects created");

    friendRepository.save(friendship1);
    System.out.println("Friendship 1 saved");

    friendRepository.save(friendship2);
    System.out.println("Friendship 2 saved");

    return "Friend request accepted successfully!";
}

    public String rejectFriendRequest(Long requestId, Long receiverId) {

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        FriendRequest request = friendRequestRepository
                .findByIdAndReceiver(requestId, receiver)
                .orElseThrow(() -> new RuntimeException("Friend request not found"));

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            return "This friend request has already been processed.";
        }

        request.setStatus(FriendRequestStatus.REJECTED);
        friendRequestRepository.save(request);

        return "Friend request rejected successfully!";
    }

    public List<FriendResponse> getFriends(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        System.out.println("Requested User ID: " + userId);
        System.out.println("Loaded User ID: " + user.getId());

        List<Friend> friends = friendRepository.findByUser(user);

        System.out.println("Friends found: " + friends.size());

        return friends.stream()
                .map(friend -> FriendResponse.builder()
                        .id(friend.getFriend().getId())
                        .username(friend.getFriend().getUsername())
                        .firstName(friend.getFriend().getFirstName())
                        .lastName(friend.getFriend().getLastName())
                        .profilePicture(friend.getFriend().getProfilePicture())
                        .build())
                .toList();
    }
}
