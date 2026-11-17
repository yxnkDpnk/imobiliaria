package com.example.primeiro_projeto.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.primeiro_projeto.models.ImovelModel;

public interface  ImovelRepository extends JpaRepository<ImovelModel, Integer>{
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select i from ImovelModel i where i.id = :id")
    java.util.Optional<ImovelModel> findForUpdate(@org.springframework.data.repository.query.Param("id") Integer id);
}
