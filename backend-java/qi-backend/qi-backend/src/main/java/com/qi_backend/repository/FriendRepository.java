package com.qi_backend.repository;

import com.qi_backend.entity.Friend;
import com.qi_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendRepository extends JpaRepository<Friend, Long> {

    List<Friend> findByUser(User user);

    @org.springframework.data.jpa.repository.Query("SELECT f FROM Friend f JOIN FETCH f.friend WHERE f.user = :user")
    List<Friend> findByUserWithFriend(@org.springframework.data.repository.query.Param("user") User user);

    void deleteByUserAndFriend(User user, User friend);

    boolean existsByUserAndFriend(User user, User friend);
}