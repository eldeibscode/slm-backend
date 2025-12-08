package com.slm.backend.dto;

import com.slm.backend.entity.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private User.Role role;
    private Boolean isArchived;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
