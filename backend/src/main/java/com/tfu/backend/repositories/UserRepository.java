package com.tfu.backend.repositories;

import com.tfu.backend.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}