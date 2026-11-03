package com.example.primeiro_projeto.repositories;

import com.example.primeiro_projeto.models.BairroModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository 
public interface BairroRepository extends JpaRepository<BairroModel, Integer>{

    boolean existsByNome(String nome);
}