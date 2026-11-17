package com.example.primeiro_projeto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import com.example.primeiro_projeto.models.*;
import com.example.primeiro_projeto.repositories.*;
import com.example.primeiro_projeto.services.*;
import com.example.primeiro_projeto.dto.NegociacaoDtos.*;

class NegociacaoServiceTests {
    private final UserRepository usuarios = mock(UserRepository.class);
    private final ImovelRepository imoveis = mock(ImovelRepository.class);
    private final OfertaRepository ofertas = mock(OfertaRepository.class);
    private final ContratoRepository contratos = mock(ContratoRepository.class);
    private final FavoritoRepository favoritos = mock(FavoritoRepository.class);
    private final EmailService emails = mock(EmailService.class);
    private NegociacaoService service;
    private UserModel cliente, admin, corretor, outroCliente;
    private ImovelModel imovel;
    private OfertaModel oferta, outraOferta;
    private ContratoModel contrato;

    @BeforeEach
    void preparar() {
        service = new NegociacaoService(usuarios, imoveis, ofertas, contratos, favoritos, emails, false);
        ReflectionTestUtils.setField(service, "entityManager", mock(jakarta.persistence.EntityManager.class));
        cliente = user(1, "cliente@email.com", "USER");
        admin = user(2, "admin@email.com", "ADMIN");
        corretor = user(3, "corretor@email.com", "CORRETOR");
        outroCliente = user(4, "outro@email.com", "USER");
        imovel = new ImovelModel(); imovel.setId(1); imovel.setTitulo("Casa"); imovel.setStatus("DISPONIVEL");
        imovel.setPrecoVenda(new BigDecimal("500000.00")); imovel.setPrecoAluguel(new BigDecimal("2000.00"));
        oferta = proposta(1, cliente); outraOferta = proposta(2, outroCliente);
        contrato = new ContratoModel(); contrato.setId(1); contrato.setOferta(oferta); contrato.setStatus("EM_APROVACAO");
        when(imoveis.findForUpdate(1)).thenReturn(Optional.of(imovel));
        when(ofertas.findById(1)).thenReturn(Optional.of(oferta));
        when(ofertas.findByImovel_IdOrderById(1)).thenReturn(List.of(oferta, outraOferta));
        when(contratos.findById(1)).thenReturn(Optional.of(contrato));
        when(ofertas.save(any())).thenAnswer(i -> i.getArgument(0));
        when(contratos.save(any())).thenAnswer(i -> i.getArgument(0));
    }
    private UserModel user(int id, String email, String tipo) {
        UserModel u = new UserModel(); u.setId(id); u.setEmail(email); u.setNome("Pessoa " + id); u.setTipo(tipo);
        when(usuarios.findByEmailIgnoreCase(email)).thenReturn(Optional.of(u)); return u;
    }
    private Authentication auth(UserModel user) { return new UsernamePasswordAuthenticationToken(user.getEmail(), null); }
    private OfertaModel proposta(int id, UserModel user) {
        OfertaModel o = new OfertaModel(); o.setId(id); o.setImovel(imovel); o.setUsuario(user);
        o.setTipo("VENDA"); o.setValorProposto(new BigDecimal("400000.00")); o.setStatus("PENDENTE"); return o;
    }
    private void erro(int status, org.junit.jupiter.api.function.Executable acao) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, acao);
        assertEquals(status, ex.getStatusCode().value());
    }

    @Test void bloqueiaOfertaEmImovelReservado() {
        imovel.setStatus("RESERVADO");
        erro(409, () -> service.criarOferta(auth(cliente), new NovaOferta(1, "VENDA", new BigDecimal("1000"), "")));
    }
    @Test void bloqueiaSegundaOfertaPendenteMesmoCliente() {
        erro(409, () -> service.criarOferta(auth(cliente), new NovaOferta(1, "VENDA", new BigDecimal("1000"), "")));
    }
    @Test void permiteOfertaDeOutroCliente() {
        UserModel novo = user(5, "novo@email.com", "USER");
        Oferta resultado = service.criarOferta(auth(novo), new NovaOferta(1, "ALUGUEL", new BigDecimal("1800"), "Proposta"));
        assertEquals("PENDENTE", resultado.status()); assertEquals("ALUGUEL", resultado.tipo());
    }
    @Test void bloqueiaValorNegativo() {
        erro(400, () -> service.criarOferta(auth(cliente), new NovaOferta(1, "VENDA", new BigDecimal("-10"), "")));
    }
    @Test void clienteNaoAceitaOfertas() { erro(403, () -> service.aceitarOferta(auth(cliente), 1)); }
    @Test void corretorAceitaReservaEPoeOutrasEmEspera() {
        Oferta aceita = service.aceitarOferta(auth(corretor), 1);
        assertEquals("ACEITA", aceita.status()); assertEquals("RESERVADO", imovel.getStatus());
        assertEquals("EM_ESPERA", outraOferta.getStatus());
    }
    @Test void naoAceitaSegundaOfertaQuandoReservado() {
        imovel.setStatus("RESERVADO"); erro(409, () -> service.aceitarOferta(auth(corretor), 1));
    }
    @Test void contratoExigeOfertaAceita() {
        erro(409, () -> service.prepararContrato(auth(corretor), new NovoContrato(1, "")));
    }
    @Test void naoCriaDoisContratosNaMesmaOferta() {
        oferta.setStatus("ACEITA"); imovel.setStatus("RESERVADO");
        when(contratos.existsByOferta_Id(1)).thenReturn(true);
        erro(409, () -> service.prepararContrato(auth(corretor), new NovoContrato(1, "")));
    }
    @Test void corretorNaoPodeAprovarContrato() { erro(403, () -> service.aprovarContrato(auth(corretor), 1)); }
    @Test void aprovacaoAdminRegistraDataMasNaoEnvia() {
        Contrato resultado = service.aprovarContrato(auth(admin), 1);
        assertEquals("AGUARDANDO_ASSINATURA", resultado.status()); assertNotNull(resultado.dataAprovacao()); assertNull(resultado.dataEnvio());
    }
    @Test void assinaturaExigeAprovacaoEEnvio() {
        contrato.setStatus("AGUARDANDO_ASSINATURA"); contrato.setDataAprovacao(LocalDateTime.now());
        erro(409, () -> service.assinarContrato(auth(cliente), 1, new Assinatura("Pessoa Um", true)));
    }
    @Test void outroClienteNaoAssinaContratoAlheio() {
        erro(403, () -> service.assinarContrato(auth(outroCliente), 1, new Assinatura("Pessoa Dois", true)));
    }
    @Test void assinaturaVendeImovelERecusaConcorrentes() {
        contrato.setStatus("AGUARDANDO_ASSINATURA"); contrato.setDataAprovacao(LocalDateTime.now()); contrato.setDataEnvio(LocalDateTime.now());
        oferta.setStatus("ACEITA"); imovel.setStatus("RESERVADO"); outraOferta.setStatus("EM_ESPERA");
        Contrato resultado = service.assinarContrato(auth(cliente), 1, new Assinatura("Pessoa Um", true));
        assertEquals("ASSINADO", resultado.status()); assertNotNull(resultado.dataAssinatura());
        assertEquals("VENDIDO", imovel.getStatus()); assertEquals("RECUSADA", outraOferta.getStatus());
    }
    @Test void assinaturaDeAluguelMarcaAlugado() {
        contrato.setStatus("AGUARDANDO_ASSINATURA"); contrato.setDataAprovacao(LocalDateTime.now()); contrato.setDataEnvio(LocalDateTime.now());
        oferta.setStatus("ACEITA"); oferta.setTipo("ALUGUEL"); imovel.setStatus("RESERVADO");
        service.assinarContrato(auth(cliente), 1, new Assinatura("Pessoa Um", true)); assertEquals("ALUGADO", imovel.getStatus());
    }
    @Test void assinaturaExigeAceiteExplicito() {
        contrato.setStatus("AGUARDANDO_ASSINATURA"); contrato.setDataAprovacao(LocalDateTime.now()); contrato.setDataEnvio(LocalDateTime.now());
        erro(400, () -> service.assinarContrato(auth(cliente), 1, new Assinatura("Pessoa Um", false)));
    }
    @Test void cancelamentoLiberaImovelEReativaEspera() {
        oferta.setStatus("ACEITA"); imovel.setStatus("RESERVADO"); outraOferta.setStatus("EM_ESPERA");
        Contrato resultado = service.cancelarContrato(auth(admin), 1);
        assertEquals("CANCELADO", resultado.status()); assertEquals("DISPONIVEL", imovel.getStatus());
        assertEquals("CANCELADA", oferta.getStatus()); assertEquals("PENDENTE", outraOferta.getStatus());
    }
    @Test void envioDisponibilizaContratoSemInventarEmailEnviado() {
        contrato.setStatus("AGUARDANDO_ASSINATURA"); contrato.setDataAprovacao(LocalDateTime.now());
        Contrato resultado = service.enviarContrato(auth(admin), 1);
        assertNotNull(resultado.dataEnvio()); assertEquals("PENDENTE", resultado.notificacaoStatus()); verifyNoInteractions(emails);
    }
    @Test void notificacaoHabilitadaEnviaEmail() {
        NegociacaoService comEmail = new NegociacaoService(usuarios, imoveis, ofertas, contratos, favoritos, emails, true);
        ReflectionTestUtils.setField(comEmail, "entityManager", mock(jakarta.persistence.EntityManager.class));
        contrato.setStatus("AGUARDANDO_ASSINATURA"); contrato.setDataAprovacao(LocalDateTime.now());
        Contrato resultado = comEmail.enviarContrato(auth(admin), 1);
        assertEquals("ENVIADO", resultado.notificacaoStatus());
        verify(emails).enviarEmailTexto(eq(cliente.getEmail()), contains("Contrato #1"), contains("área do cliente"));
    }
    @Test void falhaEmailFicaVisivelSemPerderContratoEnviado() {
        NegociacaoService comEmail = new NegociacaoService(usuarios, imoveis, ofertas, contratos, favoritos, emails, true);
        ReflectionTestUtils.setField(comEmail, "entityManager", mock(jakarta.persistence.EntityManager.class));
        contrato.setStatus("AGUARDANDO_ASSINATURA"); contrato.setDataAprovacao(LocalDateTime.now());
        doThrow(new org.springframework.mail.MailSendException("Servidor indisponível")).when(emails).enviarEmailTexto(any(), any(), any());
        Contrato resultado = comEmail.enviarContrato(auth(admin), 1);
        assertEquals("FALHA", resultado.notificacaoStatus()); assertNotNull(resultado.dataEnvio());
    }
    @Test void cancelamentoAntigoNaoLiberaOutraNegociacaoAceita() {
        outraOferta.setStatus("ACEITA");
        erro(409, () -> service.cancelarContrato(auth(admin), 1));
    }
    @Test void contratoLongoPreservaObservacoesEmVariasPaginas() {
        contrato.setObservacoes("Clausula de demonstracao.\n".repeat(100) + "CLAUSULA FINAL COMPLETA");
        String pdf = new String(ContratoPdf.gerar(contrato), java.nio.charset.Charset.forName("windows-1252"));
        assertTrue(pdf.contains("/Count 3")); assertTrue(pdf.contains("CLAUSULA FINAL COMPLETA")); assertTrue(pdf.contains("Pagina 3 de 3"));
    }
    @Test void contratoMantemDadosOriginaisQuandoCadastrosSaoEditados() {
        oferta.setStatus("ACEITA"); imovel.setStatus("RESERVADO"); imovel.setEndereco("Rua Original"); imovel.setNumero("10");
        java.util.concurrent.atomic.AtomicReference<ContratoModel> salvo = new java.util.concurrent.atomic.AtomicReference<>();
        when(contratos.save(any())).thenAnswer(i -> {
            ContratoModel c = i.getArgument(0); if (c.getId() == null) c.setId(10); salvo.set(c); return c;
        });
        service.prepararContrato(auth(corretor), new NovoContrato(1, "Dados preservados"));
        ContratoModel documento = salvo.get();
        assertNotNull(documento.getPdf());
        when(contratos.findById(10)).thenReturn(Optional.of(documento));
        imovel.setTitulo("Titulo alterado posteriormente"); imovel.setEndereco("Rua Posterior"); imovel.setNumero("99");
        cliente.setNome("Nome posterior"); cliente.setEmail("posterior@email.com");
        service.aprovarContrato(auth(admin), 10);
        String pdf = new String(service.pdf(auth(admin), 10), java.nio.charset.Charset.forName("windows-1252"));
        assertTrue(pdf.contains("Casa")); assertTrue(pdf.contains("Rua Original, 10"));
        assertTrue(pdf.contains("Pessoa 1")); assertTrue(pdf.contains("cliente@email.com"));
        assertFalse(pdf.contains("Titulo alterado posteriormente")); assertFalse(pdf.contains("Rua Posterior"));
        assertFalse(pdf.contains("Nome posterior")); assertFalse(pdf.contains("posterior@email.com"));
    }
    @Test void pdfPersistidoAposAssinaturaIncluiNomeEData() {
        contrato.setStatus("AGUARDANDO_ASSINATURA"); contrato.setDataAprovacao(LocalDateTime.now()); contrato.setDataEnvio(LocalDateTime.now());
        oferta.setStatus("ACEITA"); imovel.setStatus("RESERVADO");
        service.assinarContrato(auth(cliente), 1, new Assinatura("Pessoa Assinante", true));
        assertNotNull(contrato.getPdf());
        String pdf = new String(contrato.getPdf(), java.nio.charset.Charset.forName("windows-1252"));
        assertTrue(pdf.contains("Situacao: ASSINADO")); assertTrue(pdf.contains("Assinado por: Pessoa Assinante"));
        assertTrue(pdf.contains(contrato.getDataAssinatura().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))));
        assertArrayEquals(contrato.getPdf(), service.pdf(auth(cliente), 1));
    }
    @Test void clienteSoVeContratosEnviados() {
        when(contratos.findByOferta_Usuario_IdOrderByIdDesc(1)).thenReturn(List.of(contrato));
        assertTrue(service.listarContratos(auth(cliente)).isEmpty());
        contrato.setDataEnvio(LocalDateTime.now()); assertEquals(1, service.listarContratos(auth(cliente)).size());
    }
    @Test void clienteNaoBaixaPdfDeTerceiro() { erro(403, () -> service.pdf(auth(outroCliente), 1)); }
    @Test void pdfTemCabecalhoRodapeEAssinatura() {
        contrato.setNomeAssinatura("Pessoa Um"); contrato.setDataAssinatura(LocalDateTime.now());
        String pdf = new String(ContratoPdf.gerar(contrato), java.nio.charset.Charset.forName("windows-1252"));
        assertTrue(pdf.startsWith("%PDF-1.4")); assertTrue(pdf.contains("Pessoa Um")); assertTrue(pdf.endsWith("%%EOF\n")); assertTrue(pdf.contains("xref\n0 6"));
    }
}
