package com.slm.backend.controller;

import com.slm.backend.dto.UserDto;
import com.slm.backend.entity.User;
import com.slm.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort.Direction direction = sortDir.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));

        Page<User> userPage = userRepository.findAll(pageable);
        Page<UserDto> userDtoPage = userPage.map(this::mapToDto);

        return ResponseEntity.ok(Map.of(
            "users", userDtoPage.getContent(),
            "total", userDtoPage.getTotalElements()
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateUser(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (updates.containsKey("role")) {
            String roleStr = (String) updates.get("role");
            user.setRole(User.Role.valueOf(roleStr.toUpperCase()));
        }

        if (updates.containsKey("password")) {
            String password = (String) updates.get("password");
            user.setPassword(passwordEncoder.encode(password));
        }

        user = userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "message", "User updated successfully",
            "user", mapToDto(user)
        ));
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<Map<String, Object>> updateUserRole(
            @PathVariable Long id,
            @RequestBody Map<String, String> body
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String roleStr = body.get("role");
        user.setRole(User.Role.valueOf(roleStr.toUpperCase()));
        user = userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "message", "User role updated successfully",
            "user", mapToDto(user)
        ));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Map<String, Object>> archiveUser(
            @PathVariable Long id,
            @RequestBody Map<String, Boolean> body
    ) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Boolean isArchived = body.get("isArchived");
        user.setIsArchived(isArchived);
        user = userRepository.save(user);

        return ResponseEntity.ok(Map.of(
            "message", isArchived ? "User archived successfully" : "User unarchived successfully",
            "user", mapToDto(user)
        ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
    }

    private UserDto mapToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .role(user.getRole())
                .isArchived(user.getIsArchived())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
