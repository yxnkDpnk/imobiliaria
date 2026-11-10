package com.example.primeiro_projeto.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.primeiro_projeto.models.UserModel;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, Integer> {
    Optional<UserModel> findByEmailIgnoreCase(String email);
    boolean existsByEmailIgnoreCase(String email);
}
