package com.slm.backend.dto.auth;

import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProfileRequest {

    private String name;

    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;
}
