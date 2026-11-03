package com.example.primeiro_projeto.models;

import jakarta.persistence.*;

@Entity 
@Table(name = "bairros")
public class BairroModel{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;

    public BairroModel(){

    }
    public BairroModel(Integer id, String nome){
        this.id = id;
        this.nome = nome;
    }

    public Integer getId(){return id;}
    public void setId(Integer id){this.id = id;}

    public String getNome(){return nome;}
    public void setNome(String nome){this.nome = nome;}

    


}