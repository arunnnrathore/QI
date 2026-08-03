package com.qi_backend.repository;

import com.qi_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>
   {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findById(Long id);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

   }