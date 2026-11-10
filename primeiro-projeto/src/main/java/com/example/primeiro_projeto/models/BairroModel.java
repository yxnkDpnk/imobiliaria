package com.example.primeiro_projeto.models;

import jakarta.persistence.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity 
@Table(name = "bairros")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class BairroModel{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nome;
    private String cidade;

    @Column(length = 2)
    private String estado;

    @Column(name = "cep_inicial")
    private String cepInicial;

    @Column(name = "cep_final")
    private String cepFinal;

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

    public String getCidade(){return cidade;}
    public void setCidade(String cidade){this.cidade = cidade;}

    public String getEstado(){return estado;}
    public void setEstado(String estado){this.estado = estado;}

    public String getCepInicial(){return cepInicial;}
    public void setCepInicial(String cepInicial){this.cepInicial = cepInicial;}

    public String getCepFinal(){return cepFinal;}
    public void setCepFinal(String cepFinal){this.cepFinal = cepFinal;}

    


}
