package com.example.primeiro_projeto.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ofertas", indexes = @Index(name = "idx_oferta_imovel_status", columnList = "imovel_id,status"))
@Getter @Setter
public class OfertaModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(optional = false) @JoinColumn(name = "imovel_id", nullable = false)
    private ImovelModel imovel;
    @ManyToOne(optional = false) @JoinColumn(name = "usuario_id", nullable = false)
    private UserModel usuario;
    @Column(nullable = false)
    private String tipo;
    @Column(name = "valor_proposto", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorProposto;
    @Column(columnDefinition = "text")
    private String mensagem;
    @Column(nullable = false)
    private String status;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
