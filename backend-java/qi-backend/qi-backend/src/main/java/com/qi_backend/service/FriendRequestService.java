package com.qi_backend.service;

import com.qi_backend.entity.FriendRequest;
import com.qi_backend.entity.User;
import com.qi_backend.repository.FriendRequestRepository;
import com.qi_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import com.qi_backend.enums.FriendRequestStatus;

@Service
@RequiredArgsConstructor
public class FriendRequestService {

    private final FriendRequestRepository friendRequestRepository;
    private final UserRepository userRepository;

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
    public List<FriendRequest> getPendingRequests(Long receiverId) {

        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return friendRequestRepository.findByReceiverAndStatus(
                receiver,
                FriendRequestStatus.PENDING
        );
    }
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

        return "Friend request accepted successfully!";
    }
}