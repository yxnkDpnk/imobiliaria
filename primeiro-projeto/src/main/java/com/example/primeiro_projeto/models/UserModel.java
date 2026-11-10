package com.example.primeiro_projeto.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity // avisando o jpa q é uma tabela do banco
@Table(name = "usuarios")


public class UserModel{

    @Id //Marcar este campo como chave primária
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String nome;

    @Column(unique = true)
    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String senha;

    private String tipo;

    //construtor vazio (necessário para o spring)
    public UserModel() {}

    //construtor
    public UserModel(Integer id, String nome){
        this.id = id;
        this.nome = nome;
    }

    //getters e setters basicos

    public Integer getId(){return id;}
    public void setId(Integer id){this.id = id;}

    public String getNome(){return nome;}
    public void setNome(String nome){this.nome = nome;}

    public String getEmail(){return email;}
    public void setEmail(String email){this.email = email;}

    public String getSenha(){return senha;}
    public void setSenha(String senha){this.senha = senha;}

    public String getTipo(){return tipo;}
    public void setTipo(String tipo){this.tipo = tipo;}
}
