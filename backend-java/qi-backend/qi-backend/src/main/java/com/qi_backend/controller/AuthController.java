package com.qi_backend.controller;

import com.qi_backend.dto.RegisterRequest;
import com.qi_backend.dto.LoginRequest;
import com.qi_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.GetMapping;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request) {
        return userService.register(request);
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginRequest request) {
        return userService.login(request);
    }


    @GetMapping("/test")
    public String test() {
        return "JWT Authentication Successfully!";
    }


    @GetMapping("/profile")
    public String profile() {
        return "Welcome! You are authenticated.";
    }

}