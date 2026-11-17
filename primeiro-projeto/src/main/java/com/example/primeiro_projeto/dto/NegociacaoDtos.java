package com.example.primeiro_projeto.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import com.example.primeiro_projeto.models.ImovelModel;

public class NegociacaoDtos {
    public record NovaOferta(Integer imovelId, String tipo, BigDecimal valorProposto, String mensagem) {}
    public record NovoContrato(Integer ofertaId, String observacoes) {}
    public record Assinatura(String nomeAssinatura, Boolean aceite) {}
    public record UsuarioResumo(Integer id, String nome, String email) {}
    public record Oferta(Integer id, ImovelModel imovel, UsuarioResumo usuario, String tipo,
            BigDecimal valorProposto, String mensagem, String status, LocalDateTime createdAt) {}
    public record Contrato(Integer id, Oferta oferta, String status, String observacoes, String nomeAssinatura,
            LocalDateTime dataCriacao, LocalDateTime dataAprovacao, LocalDateTime dataEnvio,
            LocalDateTime dataAssinatura, String notificacaoStatus) {}
    public record Grupo(String nome, long total) {}
    public record Painel(long imoveisDisponiveis, long ofertasPendentes, long contratosAguardandoAssinatura,
            List<Grupo> imoveisPorBairro, List<Grupo> ofertasPorSituacao, List<Grupo> imoveisPorFinalidade) {}
}
