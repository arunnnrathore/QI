package com.qi_backend.service;

import com.qi_backend.dto.LoginRequest;
import com.qi_backend.dto.RegisterRequest;
import com.qi_backend.entity.User;
import com.qi_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.qi_backend.dto.UserResponse;
import com.qi_backend.dto.UserSearchResponse;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public String register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email already exists";
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            return "Username already exists";
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());

        userRepository.save(user);

        return "User registered successfully!";
    }

    public String login(LoginRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElse(null);

        if (user == null) {
            return "Invalid email or password";
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return "Invalid email or password";
        }

        return jwtService.generateToken(user.getEmail());
    }
    public UserResponse getProfile(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getBio(),
                user.getVerified()
        );
    }

    public List<UserSearchResponse> searchUsers(String query, String currentUserEmail) {

        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));

        List<User> matchingUsers = userRepository.searchUsers(query.trim(), currentUser.getId());

        return matchingUsers.stream()
                .map(user -> UserSearchResponse.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .email(user.getEmail())
                        .firstName(user.getFirstName())
                        .lastName(user.getLastName())
                        .profilePicture(user.getProfilePicture())
                        .bio(user.getBio())
                        .build())
                .toList();
    }
}