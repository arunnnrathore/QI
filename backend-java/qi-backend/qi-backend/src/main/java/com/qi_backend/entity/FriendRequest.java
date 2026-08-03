package com.qi_backend.entity;

import jakarta.persistence.*;
import lombok.*;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import com.qi_backend.enums.FriendRequestStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "friend_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FriendRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver;

    @Enumerated(EnumType.STRING)
    private FriendRequestStatus status;

    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
        status = FriendRequestStatus.PENDING;
    }
}