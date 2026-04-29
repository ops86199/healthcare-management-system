package com.healthcare.auth.dto;

import com.healthcare.auth.entity.User.Role;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {

    private UUID userId;
    private String firstName;
    private String lastName;
    private String email;
    private Role role;
    private String accessToken;

    @Builder.Default
    private String tokenType = "Bearer";
}
