package com.slm.backend.repository;

import com.slm.backend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepositoryCustom {
    Page<User> searchUsers(String searchTerm, Boolean isArchived, Pageable pageable);
}
