package com.qi_backend.service;

import com.qi_backend.dto.FriendRequestResponse;
import com.qi_backend.dto.FriendResponse;
import com.qi_backend.entity.Friend;
import com.qi_backend.entity.FriendRequest;
import com.qi_backend.entity.User;
import com.qi_backend.enums.FriendRequestStatus;
import com.qi_backend.repository.FriendRepository;
import com.qi_backend.repository.FriendRequestRepository;
import com.qi_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;
    private final FriendRepository friendRepository;

    public String sendFriendRequest(String senderEmail, Long receiverId) {

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        if (sender.getId().equals(receiver.getId())) {
            return "You cannot send a friend request to yourself.";
        }

        // Check if users are already friends
        if (friendRepository.existsByUserAndFriend(sender, receiver)) {
            return "You are already friends with this user.";
        }

        // Check if a PENDING request already exists from sender to receiver
        if (friendRequestRepository
                .findBySenderAndReceiverAndStatus(sender, receiver, FriendRequestStatus.PENDING)
                .isPresent()) {

            return "You have already sent a friend request to this user.";
        }

        // Check if receiver has already sent a PENDING request to sender
        if (friendRequestRepository
                .findBySenderAndReceiverAndStatus(receiver, sender, FriendRequestStatus.PENDING)
                .isPresent()) {

            return "This user has already sent you a friend request.";
        }

        FriendRequest request = FriendRequest.builder()
                .sender(sender)
                .receiver(receiver)
                .status(FriendRequestStatus.PENDING)
                .build();

        friendRequestRepository.save(request);

        return "Friend request sent successfully!";
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new RuntimeException("User is not authenticated");
        }

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));
    }

    public List<FriendRequestResponse> getPendingRequests(String receiverEmail) {

        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<FriendRequest> requests =
                friendRequestRepository.findByReceiverAndStatus(
                        receiver,
                        FriendRequestStatus.PENDING
                );

        return requests.stream()
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

    @Transactional

    public String acceptFriendRequest(
            Long requestId,
            String receiverEmail) {

        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() ->
                        new RuntimeException("Receiver not found"));

        FriendRequest request = friendRequestRepository
                .findByIdAndReceiver(requestId, receiver)
                .orElseThrow(() ->
                        new RuntimeException("Friend request not found"));

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            return "This friend request has already been processed.";
        }

        request.setStatus(FriendRequestStatus.ACCEPTED);
        friendRequestRepository.save(request);

        User sender = request.getSender();

        if (!friendRepository.existsByUserAndFriend(sender, receiver)) {

            Friend friendship1 = Friend.builder()
                    .user(sender)
                    .friend(receiver)
                    .build();

            friendRepository.save(friendship1);
        }

        if (!friendRepository.existsByUserAndFriend(receiver, sender)) {

            Friend friendship2 = Friend.builder()
                    .user(receiver)
                    .friend(sender)
                    .build();

            friendRepository.save(friendship2);
        }

        return "Friend request accepted successfully!";
    }

    @Transactional
    public String rejectFriendRequest(Long requestId) {

        User receiver = getCurrentUser();

        FriendRequest request = friendRequestRepository
                .findByIdAndReceiver(requestId, receiver)
                .orElseThrow(() ->
                        new RuntimeException("Friend request not found"));

        if (request.getStatus() != FriendRequestStatus.PENDING) {
            return "This friend request has already been processed.";
        }

        request.setStatus(FriendRequestStatus.REJECTED);
        friendRequestRepository.save(request);

        return "Friend request rejected successfully!";
    }

    public List<FriendResponse> getFriends() {

        User user = getCurrentUser();

        List<Friend> friends = friendRepository.findByUser(user);

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

    @Transactional
    public String unfriend(Long friendId) {

        User user = getCurrentUser();

        User friend = userRepository.findById(friendId)
                .orElseThrow(() ->
                        new RuntimeException("Friend not found"));

        friendRepository.deleteByUserAndFriend(user, friend);
        friendRepository.deleteByUserAndFriend(friend, user);

        return "Friend removed successfully!";
    }
}
