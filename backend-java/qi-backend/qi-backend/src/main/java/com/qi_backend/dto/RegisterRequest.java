package com.qi_backend.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RegisterRequest
{

    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
}