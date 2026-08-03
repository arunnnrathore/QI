package com.qi_backend.controller;

import com.qi_backend.dto.RegisterRequest;
import com.qi_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController
{

    private final UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody RegisterRequest request)
    {
        return userService.register(request);
    }
}