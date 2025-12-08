package com.slm.backend.dto.auth;

import com.slm.backend.dto.UserDto;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private String token;
    private UserDto user;
}
