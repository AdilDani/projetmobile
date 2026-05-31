package com.fleettracking.backend.repository;

import com.fleettracking.backend.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, String> {
    Optional<AdminUser> findByLogin(String login);
}
