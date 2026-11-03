package com.example.primeiro_projeto.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.primeiro_projeto.models.UserModel;

public interface UserRepository extends JpaRepository<UserModel, Integer> {
}