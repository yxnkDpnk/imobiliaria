package com.example.primeiro_projeto;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import com.example.primeiro_projeto.controllers.UserController;
import com.example.primeiro_projeto.models.*;
import com.example.primeiro_projeto.repositories.*;
import com.example.primeiro_projeto.services.*;

class CrudRegrasTests {
    @Test
    void administradorNaoPodeSeExcluirOuSeRebaixar() {
        UserService service = mock(UserService.class);
        UserModel admin = new UserModel(1, "Administrador");
        admin.setEmail("admin@imobiliaria.com");
        admin.setTipo("ADMIN");
        when(service.findAll()).thenReturn(List.of(admin));
        UserController controller = new UserController(service);
        var auth = new UsernamePasswordAuthenticationToken(admin.getEmail(), null);
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class, () -> controller.deleteUser(1, auth)).getStatusCode());
        UserModel dados = new UserModel();
        dados.setTipo("USER");
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class, () -> controller.updateUser(1, dados, auth)).getStatusCode());
        verify(service, never()).delete(any());
        verify(service, never()).update(any(), any());
    }

    @Test
    void cadastroENovaAlteracaoManualNaoPodemBurlarStatusDaNegociacao() {
        ImovelRepository repository = mock(ImovelRepository.class);
        ImovelService service = new ImovelService(repository);
        ImovelModel novo = new ImovelModel();
        novo.setStatus("VENDIDO");
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class, () -> service.insert(novo)).getStatusCode());
        ImovelModel reservado = new ImovelModel();
        reservado.setId(1);
        reservado.setStatus("RESERVADO");
        when(repository.findForUpdate(1)).thenReturn(Optional.of(reservado));
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class, () -> service.updateStatus(1, "DISPONIVEL")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT,
                assertThrows(ResponseStatusException.class, () -> service.delete(1)).getStatusCode());
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any());
    }

    @Test
    void editarImovelSomenteParaAluguelRemovePrecoDeVendaZerado() {
        ImovelRepository repository = mock(ImovelRepository.class);
        BairroRepository bairros = mock(BairroRepository.class);
        TipoImovelRepository tipos = mock(TipoImovelRepository.class);
        ImovelService service = new ImovelService(repository);
        ReflectionTestUtils.setField(service, "bairros", bairros);
        ReflectionTestUtils.setField(service, "tipos", tipos);
        BairroModel bairro = new BairroModel(1, "Centro");
        TipoImovelModel tipo = new TipoImovelModel();
        tipo.setId(1);
        tipo.setNome("Casa");
        ImovelModel atual = new ImovelModel();
        atual.setId(1);
        atual.setTitulo("Casa com jardim");
        atual.setStatus("DISPONIVEL");
        atual.setFinalidade("VENDA_ALUGUEL");
        atual.setPrecoVenda(new BigDecimal("850000.00"));
        atual.setPrecoAluguel(new BigDecimal("3500.00"));
        atual.setBairro(bairro);
        atual.setTipoImovel(tipo);
        when(repository.findForUpdate(1)).thenReturn(Optional.of(atual));
        when(bairros.existsById(1)).thenReturn(true);
        when(bairros.findById(1)).thenReturn(Optional.of(bairro));
        when(tipos.existsById(1)).thenReturn(true);
        when(tipos.findById(1)).thenReturn(Optional.of(tipo));
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ImovelModel edicao = new ImovelModel();
        edicao.setId(1);
        edicao.setFinalidade("ALUGUEL");
        edicao.setPrecoVenda(BigDecimal.ZERO);
        ImovelModel salvo = service.update(edicao);
        assertNull(salvo.getPrecoVenda(), "Preço removido não deve aparecer como uma venda por zero reais.");
        assertEquals(new BigDecimal("3500.00"), salvo.getPrecoAluguel());
        assertEquals("ALUGUEL", salvo.getFinalidade());
        assertEquals("Casa com jardim", salvo.getTitulo());
        verify(repository).save(atual);
    }

    @Test
    void bairroDuplicadoNaMesmaCidadeRetornaConflito() {
        BairroRepository repository = mock(BairroRepository.class);
        BairroModel existente = new BairroModel(1, "Centro");
        existente.setCidade("Porto Alegre");
        existente.setEstado("RS");
        when(repository.findAll()).thenReturn(List.of(existente));
        BairroModel novo = new BairroModel(null, " centro ");
        novo.setCidade("Porto Alegre");
        novo.setEstado("rs");
        assertEquals(HttpStatus.CONFLICT, assertThrows(ResponseStatusException.class,
                () -> new BairroService(repository).insert(novo)).getStatusCode());
        verify(repository, never()).save(any());
    }

    @Test
    void uploadValidaConteudoESalvaComNomeSeguro(@TempDir Path pasta) throws Exception {
        ImovelRepository imoveis = mock(ImovelRepository.class);
        FotoImovelRepository fotos = mock(FotoImovelRepository.class);
        ImovelModel imovel = new ImovelModel();
        imovel.setId(1);
        when(imoveis.findById(1)).thenReturn(Optional.of(imovel));
        when(fotos.findByImovel_Id(1)).thenReturn(List.of());
        when(fotos.saveAndFlush(any())).thenAnswer(invocation -> invocation.getArgument(0));
        FotoImovelService service = new FotoImovelService();
        ReflectionTestUtils.setField(service, "imovelRepository", imoveis);
        ReflectionTestUtils.setField(service, "fotoImovelRepository", fotos);
        ReflectionTestUtils.setField(service, "uploadDir", pasta.toString());
        MockMultipartFile falso = new MockMultipartFile("arquivo", "foto.png", "image/png", "não é imagem".getBytes());
        assertEquals(HttpStatus.BAD_REQUEST,
                assertThrows(ResponseStatusException.class, () -> service.salvar(falso, 1)).getStatusCode());
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "png", bytes);
        FotoImovelModel salvo = service.salvar(new MockMultipartFile("arquivo", "../../foto.png", "image/png", bytes.toByteArray()), 1);
        assertFalse(salvo.getNomeArquivo().contains("/"));
        assertTrue(Files.isRegularFile(pasta.resolve("1").resolve(salvo.getNomeArquivo())));
        assertTrue(salvo.getCapa());
    }
}
