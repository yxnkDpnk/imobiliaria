package com.example.primeiro_projeto.repositories;

import com.example.primeiro_projeto.models.TipoImovelModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TipoImovelRepository extends JpaRepository<TipoImovelModel, Integer> {

    boolean existsByNome(String nome);
}