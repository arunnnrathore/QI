package com.qi_backend.controller;

import com.qi_backend.dto.ChangePasswordRequest;
import com.qi_backend.dto.UpdateProfileRequest;
import com.qi_backend.dto.UserResponse;
import com.qi_backend.dto.UserSearchResponse;
import com.qi_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // Get my full profile
    @GetMapping("/me")
    public UserResponse getMyProfile(Authentication authentication) {
        String email = authentication.getName();
        return userService.getProfile(email);
    }

    // Get any user's profile by ID
    @GetMapping("/{id}")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // Update profile (bio, avatar, first/last name, preferred language)
    @PutMapping("/profile")
    public UserResponse updateProfile(
            @RequestBody UpdateProfileRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        return userService.updateProfile(email, request);
    }

    // Change password
    @PutMapping("/change-password")
    public String changePassword(
            @RequestBody ChangePasswordRequest request,
            Authentication authentication) {

        String email = authentication.getName();
        return userService.changePassword(email, request);
    }

    // Search users by keyword
    @GetMapping("/search")
    public List<UserSearchResponse> searchUsers(
            @RequestParam(required = false, defaultValue = "") String query,
            Authentication authentication) {

        String currentUserEmail = authentication.getName();
        return userService.searchUsers(query, currentUserEmail);
    }
}
