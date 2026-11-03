package com.example.primeiro_projeto.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.primeiro_projeto.models.ImovelModel;

public interface  ImovelRepository extends JpaRepository<ImovelModel, Integer>{
    
}