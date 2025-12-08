package com.slm.backend.repository;

import com.slm.backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long>, UserRepositoryCustom {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
}
