package com.example.primeiro_projeto.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "imoveis")
@Getter
@Setter
public class ImovelModel implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String titulo;

    @Column(columnDefinition = "text")
    private String descricao;

    @Column(name = "preco_venda", precision = 15, scale = 2)
    private BigDecimal precoVenda;

    @Column(name = "preco_aluguel", precision = 15, scale = 2)
    private BigDecimal precoAluguel;


    private String finalidade;


    private String status;

    private Integer dormitorios;

    private Integer banheiros;

    private Integer garagem;

    @Column(name = "area_total", precision = 10, scale = 2)
    private BigDecimal areaTotal;

    @Column(name = "area_construida", precision = 10, scale = 2)
    private BigDecimal areaConstruida;

    private String endereco;

    private String numero;

    private String complemento;

    private String cep;

    @Column(columnDefinition = "text")
    private String caracteristicas;

    private Boolean destaque;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_imovel_id")
    private TipoImovelModel tipoImovel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bairro_id")
    private BairroModel bairro;

    @ManyToOne
    @JoinColumn(name = "usuario_id")
    private UserModel usuario;



 






    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}