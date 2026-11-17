package com.example.primeiro_projeto.services;

import static com.example.primeiro_projeto.dto.NegociacaoDtos.*;
import static org.springframework.http.HttpStatus.*;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.primeiro_projeto.models.*;
import com.example.primeiro_projeto.repositories.*;

@Service
@Transactional(isolation = org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
public class NegociacaoService {
    private final UserRepository usuarios;
    private final ImovelRepository imoveis;
    private final OfertaRepository ofertas;
    private final ContratoRepository contratos;
    private final FavoritoRepository favoritos;
    private final EmailService emails;
    private final boolean notificacoesEmail;
    @jakarta.persistence.PersistenceContext
    private jakarta.persistence.EntityManager entityManager;

    public NegociacaoService(UserRepository usuarios, ImovelRepository imoveis, OfertaRepository ofertas,
            ContratoRepository contratos, FavoritoRepository favoritos, EmailService emails,
            @Value("${app.notificacoes-email:false}") boolean notificacoesEmail) {
        this.usuarios = usuarios; this.imoveis = imoveis; this.ofertas = ofertas;
        this.contratos = contratos; this.favoritos = favoritos; this.emails = emails;
        this.notificacoesEmail = notificacoesEmail;
    }

    public UserModel usuario(Authentication auth) {
        if (auth == null) throw erro(UNAUTHORIZED, "Entre para continuar.");
        return usuarios.findByEmailIgnoreCase(auth.getName())
                .orElseThrow(() -> erro(UNAUTHORIZED, "Sua conta não está mais disponível."));
    }

    private void perfil(UserModel usuario, String... tipos) {
        if (!Arrays.asList(tipos).contains(usuario.getTipo())) throw erro(FORBIDDEN, "Acesso negado para este perfil.");
    }

    @Transactional(readOnly = true)
    public List<Oferta> listarOfertas(Authentication auth, String status) {
        UserModel user = usuario(auth);
        List<OfertaModel> lista = "USER".equals(user.getTipo())
                ? ofertas.findByUsuario_IdOrderByIdDesc(user.getId()) : ofertas.findAllByOrderByIdDesc();
        return lista.stream().filter(o -> status == null || status.isBlank() || o.getStatus().equals(normalizar(status)))
                .map(this::ofertaDto).toList();
    }

    public Oferta criarOferta(Authentication auth, NovaOferta dados) {
        UserModel user = usuario(auth);
        perfil(user, "USER");
        if (dados == null || dados.imovelId() == null || dados.tipo() == null || dados.valorProposto() == null)
            throw erro(BAD_REQUEST, "Informe imóvel, tipo e valor da oferta.");
        String tipo = normalizar(dados.tipo());
        if (!Set.of("VENDA", "ALUGUEL").contains(tipo)) throw erro(BAD_REQUEST, "Tipo da oferta deve ser VENDA ou ALUGUEL.");
        BigDecimal valor = dados.valorProposto();
        if (valor.signum() <= 0 || valor.compareTo(new BigDecimal("9999999999999.99")) > 0 || valor.scale() > 2)
            throw erro(BAD_REQUEST, "Informe um valor positivo, com até duas casas decimais.");
        if (dados.mensagem() != null && dados.mensagem().length() > 3000) throw erro(BAD_REQUEST, "A mensagem deve ter até 3000 caracteres.");
        ImovelModel imovel = bloquearImovel(dados.imovelId());
        if (!normalizar(imovel.getStatus()).equals("DISPONIVEL")) throw erro(CONFLICT, "Somente imóveis disponíveis recebem ofertas.");
        if ((tipo.equals("VENDA") && (imovel.getPrecoVenda() == null || imovel.getPrecoVenda().signum() <= 0))
                || (tipo.equals("ALUGUEL") && (imovel.getPrecoAluguel() == null || imovel.getPrecoAluguel().signum() <= 0)))
            throw erro(CONFLICT, "Este imóvel não está anunciado para essa finalidade.");
        List<OfertaModel> existentes = ofertas.findByImovel_IdOrderById(imovel.getId());
        if (existentes.stream().anyMatch(o -> o.getUsuario().getId().equals(user.getId()) && o.getStatus().equals("PENDENTE")))
            throw erro(CONFLICT, "Você já possui uma oferta pendente para este imóvel.");
        OfertaModel oferta = new OfertaModel();
        oferta.setImovel(imovel); oferta.setUsuario(user); oferta.setTipo(tipo); oferta.setValorProposto(valor);
        oferta.setMensagem(dados.mensagem() == null ? "" : dados.mensagem().trim()); oferta.setStatus("PENDENTE");
        return ofertaDto(ofertas.save(oferta));
    }

    public Oferta aceitarOferta(Authentication auth, Integer id) {
        perfil(usuario(auth), "ADMIN", "CORRETOR");
        OfertaModel oferta = localizarOferta(id);
        ImovelModel imovel = bloquearImovel(oferta.getImovel().getId());
        entityManager.refresh(oferta);
        // Recarrega sob o bloqueio do imóvel para observar a decisão da transação anterior.
        List<OfertaModel> relacionadas = ofertas.findByImovel_IdOrderById(imovel.getId());
        oferta = relacionadas.stream().filter(o -> o.getId().equals(id)).findFirst().orElseThrow();
        if (!oferta.getStatus().equals("PENDENTE")) throw erro(CONFLICT, "Somente ofertas pendentes podem ser aceitas.");
        if (!normalizar(imovel.getStatus()).equals("DISPONIVEL")) throw erro(CONFLICT, "O imóvel já está reservado ou negociado.");
        oferta.setStatus("ACEITA"); imovel.setStatus("RESERVADO");
        for (OfertaModel outra : relacionadas) {
            if (!outra.getId().equals(id) && outra.getStatus().equals("PENDENTE")) outra.setStatus("EM_ESPERA");
        }
        ofertas.saveAll(relacionadas); imoveis.save(imovel);
        return ofertaDto(oferta);
    }

    public Oferta recusarOferta(Authentication auth, Integer id) {
        perfil(usuario(auth), "ADMIN", "CORRETOR");
        OfertaModel oferta = localizarOferta(id);
        bloquearImovel(oferta.getImovel().getId());
        entityManager.refresh(oferta);
        if (!Set.of("PENDENTE", "EM_ESPERA").contains(oferta.getStatus())) throw erro(CONFLICT, "Esta oferta não pode mais ser recusada. Cancele o contrato se necessário.");
        oferta.setStatus("RECUSADA"); return ofertaDto(ofertas.save(oferta));
    }

    public Oferta cancelarOferta(Authentication auth, Integer id) {
        UserModel user = usuario(auth);
        OfertaModel oferta = localizarOferta(id);
        if (user.getTipo().equals("USER") && !oferta.getUsuario().getId().equals(user.getId())) throw erro(FORBIDDEN, "Esta oferta pertence a outro cliente.");
        bloquearImovel(oferta.getImovel().getId());
        entityManager.refresh(oferta);
        if (!Set.of("PENDENTE", "EM_ESPERA").contains(oferta.getStatus())) throw erro(CONFLICT, "Só é possível cancelar ofertas pendentes ou em espera. Para desistir de um contrato, contate o administrador.");
        oferta.setStatus("CANCELADA"); return ofertaDto(ofertas.save(oferta));
    }

    @Transactional(readOnly = true)
    public List<ImovelModel> listarFavoritos(Authentication auth) {
        UserModel user = usuario(auth); perfil(user, "USER");
        return favoritos.findByUsuario_IdOrderByIdDesc(user.getId()).stream().map(FavoritoModel::getImovel).toList();
    }

    public void adicionarFavorito(Authentication auth, Integer imovelId) {
        UserModel user = usuario(auth); perfil(user, "USER");
        ImovelModel imovel = bloquearImovel(imovelId);
        if (favoritos.findByUsuario_IdAndImovel_Id(user.getId(), imovelId).isEmpty()) {
            FavoritoModel favorito = new FavoritoModel(); favorito.setUsuario(user); favorito.setImovel(imovel);
            favoritos.save(favorito);
        }
    }

    public void removerFavorito(Authentication auth, Integer imovelId) {
        UserModel user = usuario(auth); perfil(user, "USER");
        favoritos.findByUsuario_IdAndImovel_Id(user.getId(), imovelId).ifPresent(favoritos::delete);
    }

    @Transactional(readOnly = true)
    public List<Contrato> listarContratos(Authentication auth) {
        UserModel user = usuario(auth);
        List<ContratoModel> lista = user.getTipo().equals("USER")
                ? contratos.findByOferta_Usuario_IdOrderByIdDesc(user.getId()) : contratos.findAllByOrderByIdDesc();
        return lista.stream().filter(c -> !user.getTipo().equals("USER") || c.getDataEnvio() != null).map(this::contratoDto).toList();
    }

    public Contrato prepararContrato(Authentication auth, NovoContrato dados) {
        perfil(usuario(auth), "ADMIN", "CORRETOR");
        if (dados == null || dados.ofertaId() == null) throw erro(BAD_REQUEST, "Informe a oferta aceita.");
        if (dados.observacoes() != null && dados.observacoes().length() > 6000) throw erro(BAD_REQUEST, "As observações devem ter até 6000 caracteres.");
        OfertaModel oferta = localizarOferta(dados.ofertaId());
        ImovelModel imovel = bloquearImovel(oferta.getImovel().getId());
        entityManager.refresh(oferta);
        if (!oferta.getStatus().equals("ACEITA") || !normalizar(imovel.getStatus()).equals("RESERVADO")) throw erro(CONFLICT, "O contrato exige uma oferta aceita e um imóvel reservado.");
        if (contratos.existsByOferta_Id(oferta.getId())) throw erro(CONFLICT, "Esta oferta já possui contrato.");
        ContratoModel contrato = new ContratoModel(); contrato.setOferta(oferta); contrato.setStatus("EM_APROVACAO");
        contrato.setObservacoes(dados.observacoes() == null ? "" : dados.observacoes().trim());
        contrato.setTituloImovelSnapshot(imovel.getTitulo() == null ? "" : imovel.getTitulo());
        contrato.setEnderecoImovelSnapshot(imovel.getEndereco() == null ? "" : imovel.getEndereco());
        contrato.setNumeroImovelSnapshot(imovel.getNumero() == null ? "" : imovel.getNumero());
        contrato.setNomeClienteSnapshot(oferta.getUsuario().getNome() == null ? "" : oferta.getUsuario().getNome());
        contrato.setEmailClienteSnapshot(oferta.getUsuario().getEmail() == null ? "" : oferta.getUsuario().getEmail());
        return salvarContrato(contrato);
    }

    public Contrato aprovarContrato(Authentication auth, Integer id) {
        perfil(usuario(auth), "ADMIN");
        ContratoModel contrato = bloquearContrato(id);
        exigirStatus(contrato, "EM_APROVACAO");
        contrato.setStatus("AGUARDANDO_ASSINATURA"); contrato.setDataAprovacao(LocalDateTime.now());
        return salvarContrato(contrato);
    }

    public Contrato enviarContrato(Authentication auth, Integer id) {
        perfil(usuario(auth), "ADMIN");
        ContratoModel contrato = bloquearContrato(id); exigirStatus(contrato, "AGUARDANDO_ASSINATURA");
        if (contrato.getDataAprovacao() == null) throw erro(CONFLICT, "Aprove o contrato antes de enviar.");
        contrato.setDataEnvio(LocalDateTime.now());
        contrato.setNotificacaoStatus("PENDENTE");
        if (notificacoesEmail) {
            try {
                emails.enviarEmailTexto(contrato.getOferta().getUsuario().getEmail(),
                        "Contrato #" + contrato.getId() + " disponível para assinatura",
                        "Seu contrato do imóvel #" + contrato.getOferta().getImovel().getId()
                        + " foi aprovado. Entre na área do cliente da imobiliária para consultar o PDF e assinar.");
                contrato.setNotificacaoStatus("ENVIADO");
            } catch (org.springframework.mail.MailException ex) {
                contrato.setNotificacaoStatus("FALHA");
            }
        }
        return salvarContrato(contrato);
    }

    public Contrato assinarContrato(Authentication auth, Integer id, Assinatura dados) {
        UserModel user = usuario(auth); perfil(user, "USER");
        ContratoModel contrato = bloquearContrato(id);
        if (!contrato.getOferta().getUsuario().getId().equals(user.getId())) throw erro(FORBIDDEN, "Este contrato pertence a outro cliente.");
        exigirStatus(contrato, "AGUARDANDO_ASSINATURA");
        if (contrato.getDataAprovacao() == null || contrato.getDataEnvio() == null) throw erro(CONFLICT, "O contrato precisa ser aprovado e enviado antes da assinatura.");
        if (dados == null || !Boolean.TRUE.equals(dados.aceite()) || dados.nomeAssinatura() == null || dados.nomeAssinatura().trim().length() < 3 || dados.nomeAssinatura().length() > 255)
            throw erro(BAD_REQUEST, "Informe seu nome e confirme que leu e aceita o contrato.");
        if (!contrato.getOferta().getStatus().equals("ACEITA")) throw erro(CONFLICT, "A oferta não está mais aceita.");
        ImovelModel imovel = contrato.getOferta().getImovel();
        if (!normalizar(imovel.getStatus()).equals("RESERVADO")) throw erro(CONFLICT, "O imóvel não está reservado para esta negociação.");
        contrato.setStatus("ASSINADO"); contrato.setDataAssinatura(LocalDateTime.now()); contrato.setNomeAssinatura(dados.nomeAssinatura().trim());
        imovel.setStatus(contrato.getOferta().getTipo().equals("ALUGUEL") ? "ALUGADO" : "VENDIDO");
        List<OfertaModel> relacionadas = ofertas.findByImovel_IdOrderById(imovel.getId());
        for (OfertaModel outra : relacionadas) {
            if (!outra.getId().equals(contrato.getOferta().getId()) && Set.of("PENDENTE", "EM_ESPERA").contains(outra.getStatus())) outra.setStatus("RECUSADA");
        }
        ofertas.saveAll(relacionadas); imoveis.save(imovel);
        return salvarContrato(contrato);
    }

    public Contrato recusarContrato(Authentication auth, Integer id) {
        perfil(usuario(auth), "ADMIN");
        ContratoModel contrato = bloquearContrato(id); exigirStatus(contrato, "EM_APROVACAO");
        contrato.setStatus("RECUSADO"); liberarImovel(contrato, "RECUSADA");
        return salvarContrato(contrato);
    }

    public Contrato cancelarContrato(Authentication auth, Integer id) {
        perfil(usuario(auth), "ADMIN");
        ContratoModel contrato = bloquearContrato(id);
        if (Set.of("CANCELADO", "RECUSADO").contains(contrato.getStatus())) throw erro(CONFLICT, "Este contrato já está encerrado.");
        contrato.setStatus("CANCELADO"); liberarImovel(contrato, "CANCELADA");
        return salvarContrato(contrato);
    }

    private void liberarImovel(ContratoModel contrato, String statusOferta) {
        ImovelModel imovel = contrato.getOferta().getImovel();
        List<OfertaModel> relacionadas = ofertas.findByImovel_IdOrderById(imovel.getId());
        if (relacionadas.stream().anyMatch(o -> !o.getId().equals(contrato.getOferta().getId()) && o.getStatus().equals("ACEITA")))
            throw erro(CONFLICT, "Existe outra negociação aceita para este imóvel. Confira o contrato atual antes de cancelar.");
        contrato.getOferta().setStatus(statusOferta); ofertas.save(contrato.getOferta());
        imovel.setStatus("DISPONIVEL"); imoveis.save(imovel);
        relacionadas.stream().filter(o -> o.getStatus().equals("EM_ESPERA")).forEach(o -> o.setStatus("PENDENTE"));
        ofertas.saveAll(relacionadas);
    }

    @Transactional(readOnly = true)
    public byte[] pdf(Authentication auth, Integer id) {
        UserModel user = usuario(auth);
        ContratoModel contrato = contratos.findById(id).orElseThrow(() -> erro(NOT_FOUND, "Contrato não encontrado."));
        if (user.getTipo().equals("USER") && (!contrato.getOferta().getUsuario().getId().equals(user.getId()) || contrato.getDataEnvio() == null))
            throw erro(FORBIDDEN, "Você não tem acesso a este contrato.");
        return contrato.getPdf() == null ? ContratoPdf.gerar(contrato) : contrato.getPdf();
    }

    @Transactional(readOnly = true)
    public Painel painel(Authentication auth) {
        perfil(usuario(auth), "ADMIN", "CORRETOR");
        List<ImovelModel> listaImoveis = imoveis.findAll();
        List<OfertaModel> listaOfertas = ofertas.findAll();
        List<ContratoModel> listaContratos = contratos.findAll();
        return new Painel(listaImoveis.stream().filter(i -> normalizar(i.getStatus()).equals("DISPONIVEL")).count(),
                listaOfertas.stream().filter(o -> o.getStatus().equals("PENDENTE")).count(),
                listaContratos.stream().filter(c -> c.getStatus().equals("AGUARDANDO_ASSINATURA")).count(),
                agrupar(listaImoveis, i -> i.getBairro() == null ? "Não informado" : i.getBairro().getNome()),
                agrupar(listaOfertas, OfertaModel::getStatus),
                agrupar(listaImoveis, i -> i.getPrecoVenda() != null && i.getPrecoAluguel() != null ? "VENDA E ALUGUEL" : i.getPrecoVenda() != null ? "VENDA" : "ALUGUEL"));
    }

    private <T> List<Grupo> agrupar(List<T> valores, Function<T, String> chave) {
        return valores.stream().collect(Collectors.groupingBy(chave, TreeMap::new, Collectors.counting()))
                .entrySet().stream().map(e -> new Grupo(e.getKey(), e.getValue())).toList();
    }

    private OfertaModel localizarOferta(Integer id) { return ofertas.findById(id).orElseThrow(() -> erro(NOT_FOUND, "Oferta não encontrada.")); }
    private ImovelModel bloquearImovel(Integer id) {
        ImovelModel imovel = imoveis.findForUpdate(id).orElseThrow(() -> erro(NOT_FOUND, "Imóvel não encontrado."));
        entityManager.refresh(imovel);
        return imovel;
    }
    private ContratoModel bloquearContrato(Integer id) {
        ContratoModel contrato = contratos.findById(id).orElseThrow(() -> erro(NOT_FOUND, "Contrato não encontrado."));
        bloquearImovel(contrato.getOferta().getImovel().getId());
        entityManager.refresh(contrato);
        entityManager.refresh(contrato.getOferta());
        return contrato;
    }
    private void exigirStatus(ContratoModel contrato, String status) {
        if (!contrato.getStatus().equals(status)) throw erro(CONFLICT, "Esta ação não é permitida na situação atual do contrato.");
    }
    private Oferta ofertaDto(OfertaModel o) {
        return new Oferta(o.getId(), o.getImovel(), new UsuarioResumo(o.getUsuario().getId(), o.getUsuario().getNome(), o.getUsuario().getEmail()),
                o.getTipo(), o.getValorProposto(), o.getMensagem(), o.getStatus(), o.getCreatedAt());
    }
    private Contrato contratoDto(ContratoModel c) {
        return new Contrato(c.getId(), ofertaDto(c.getOferta()), c.getStatus(), c.getObservacoes(), c.getNomeAssinatura(),
                c.getDataCriacao(), c.getDataAprovacao(), c.getDataEnvio(), c.getDataAssinatura(), c.getNotificacaoStatus());
    }
    private Contrato salvarContrato(ContratoModel contrato) {
        ContratoModel salvo = contratos.save(contrato);
        salvo.setPdf(ContratoPdf.gerar(salvo));
        return contratoDto(contratos.save(salvo));
    }
    static String normalizar(String texto) {
        return texto == null ? "" : Normalizer.normalize(texto, Normalizer.Form.NFD).replaceAll("\\p{M}", "").trim().toUpperCase(Locale.ROOT);
    }
    private ResponseStatusException erro(org.springframework.http.HttpStatus status, String mensagem) { return new ResponseStatusException(status, mensagem); }
}
