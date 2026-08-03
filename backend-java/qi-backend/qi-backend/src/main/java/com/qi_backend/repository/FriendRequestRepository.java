package com.qi_backend.repository;

import com.qi_backend.entity.FriendRequest;
import com.qi_backend.entity.User;
import com.qi_backend.enums.FriendRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendRequestRepository extends JpaRepository<FriendRequest, Long>
{

    Optional<FriendRequest> findBySenderAndReceiver(User sender, User receiver);

    List<FriendRequest> findByReceiverAndStatus(User receiver, FriendRequestStatus status);

    Optional<FriendRequest> findByIdAndReceiver(Long id, User receiver);
}