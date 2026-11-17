package com.example.primeiro_projeto.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity @Table(name = "contratos")
@Getter @Setter
public class ContratoModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @OneToOne(optional = false) @JoinColumn(name = "oferta_id", nullable = false, unique = true)
    private OfertaModel oferta;
    @Column(nullable = false)
    private String status;
    @Column(columnDefinition = "text")
    private String observacoes;
    private String nomeAssinatura;
    private String tituloImovelSnapshot;
    private String enderecoImovelSnapshot;
    private String numeroImovelSnapshot;
    private String nomeClienteSnapshot;
    private String emailClienteSnapshot;
    @Lob
    @Column(columnDefinition = "LONGBLOB")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private byte[] pdf;
    @Column(nullable = false, updatable = false)
    private LocalDateTime dataCriacao = LocalDateTime.now();
    private LocalDateTime dataAprovacao;
    private LocalDateTime dataEnvio;
    private LocalDateTime dataAssinatura;
    @Column(nullable = false)
    private String notificacaoStatus = "PENDENTE";
}
