package com.qi_backend.repository;

import com.qi_backend.entity.Friend;
import com.qi_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findByUser(User user);

    void deleteByUserAndFriend(User user, User friend);

    boolean existsByUserAndFriend(User user, User friend);
}