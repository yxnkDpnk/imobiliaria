package com.example.primeiro_projeto.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.primeiro_projeto.models.FotoImovelModel;

public interface  FotoImovelRepository extends JpaRepository
		<FotoImovelModel, Integer>{

	List<FotoImovelModel> findByImovel_Id(Integer imovelId);
}