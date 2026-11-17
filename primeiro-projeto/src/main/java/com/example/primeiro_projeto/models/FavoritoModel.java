package com.example.primeiro_projeto.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "favoritos", uniqueConstraints = @UniqueConstraint(name = "uk_favorito_usuario_imovel", columnNames = {"usuario_id", "imovel_id"}))
@Getter @Setter
public class FavoritoModel {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @ManyToOne(optional = false) @JoinColumn(name = "usuario_id", nullable = false)
    private UserModel usuario;
    @ManyToOne(optional = false) @JoinColumn(name = "imovel_id", nullable = false)
    private ImovelModel imovel;
}
