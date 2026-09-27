package com.qi_backend.controller;

import com.qi_backend.dto.UserSearchResponse;
import com.qi_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/search")
    public List<UserSearchResponse> searchUsers(
            @RequestParam(required = false, defaultValue = "") String query,
            Authentication authentication) {

        String currentUserEmail = authentication.getName();
        return userService.searchUsers(query, currentUserEmail);
    }
}
