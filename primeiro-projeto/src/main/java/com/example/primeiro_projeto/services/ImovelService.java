package com.example.primeiro_projeto.services;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.util.List;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.primeiro_projeto.models.ImovelModel;
import com.example.primeiro_projeto.repositories.BairroRepository;
import com.example.primeiro_projeto.repositories.ImovelRepository;
import com.example.primeiro_projeto.repositories.TipoImovelRepository;

@Service
public class ImovelService {
    private final ImovelRepository repository;
    @Autowired private BairroRepository bairros;
    @Autowired private TipoImovelRepository tipos;

    public ImovelService(ImovelRepository repository) { this.repository = repository; }
    public List<ImovelModel> getAll() { return repository.findAll(); }
    public ImovelModel find(Integer id) { return repository.findById(id).orElse(null); }

    @Transactional
    public ImovelModel insert(ImovelModel model) {
        if (model.getStatus() != null && !normalizar(model.getStatus()).equals("DISPONIVEL")) {
            throw conflito("Um imóvel novo deve começar disponível.");
        }
        model.setId(null);
        model.setStatus("DISPONIVEL");
        validar(model);
        return repository.save(model);
    }

    @Transactional
    public ImovelModel update(ImovelModel dados) {
        ImovelModel atual = exigir(dados.getId());
        if (dados.getStatus() != null && !normalizar(dados.getStatus()).equals(normalizar(atual.getStatus()))) {
            throw conflito("O status do imóvel é atualizado pelo fluxo de ofertas e contratos.");
        }
        if (dados.getTitulo() != null) atual.setTitulo(dados.getTitulo());
        if (dados.getDescricao() != null) atual.setDescricao(dados.getDescricao());
        if (dados.getPrecoVenda() != null) atual.setPrecoVenda(dados.getPrecoVenda());
        if (dados.getPrecoAluguel() != null) atual.setPrecoAluguel(dados.getPrecoAluguel());
        if (dados.getFinalidade() != null) atual.setFinalidade(dados.getFinalidade());
        if (dados.getDormitorios() != null) atual.setDormitorios(dados.getDormitorios());
        if (dados.getBanheiros() != null) atual.setBanheiros(dados.getBanheiros());
        if (dados.getGaragem() != null) atual.setGaragem(dados.getGaragem());
        if (dados.getAreaTotal() != null) atual.setAreaTotal(dados.getAreaTotal());
        if (dados.getAreaConstruida() != null) atual.setAreaConstruida(dados.getAreaConstruida());
        if (dados.getEndereco() != null) atual.setEndereco(dados.getEndereco());
        if (dados.getNumero() != null) atual.setNumero(dados.getNumero());
        if (dados.getComplemento() != null) atual.setComplemento(dados.getComplemento());
        if (dados.getCep() != null) atual.setCep(dados.getCep());
        if (dados.getCaracteristicas() != null) atual.setCaracteristicas(dados.getCaracteristicas());
        if (dados.getDestaque() != null) atual.setDestaque(dados.getDestaque());
        if (dados.getTipoImovel() != null) atual.setTipoImovel(dados.getTipoImovel());
        if (dados.getBairro() != null) atual.setBairro(dados.getBairro());
        validar(atual);
        return repository.save(atual);
    }

    @Transactional
    public ImovelModel updateStatus(Integer id, String status) {
        ImovelModel imovel = exigir(id);
        if (!normalizar(status).equals(normalizar(imovel.getStatus()))) {
            throw conflito("Altere o status pelo aceite da oferta, assinatura ou cancelamento do contrato.");
        }
        return imovel;
    }

    @Transactional
    public void delete(Integer id) {
        ImovelModel imovel = exigir(id);
        if (!normalizar(imovel.getStatus()).equals("DISPONIVEL")) {
            throw conflito("Um imóvel reservado, alugado ou vendido não pode ser excluído.");
        }
        repository.delete(imovel);
        repository.flush();
    }

    private ImovelModel exigir(Integer id) {
        if (id == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o código do imóvel.");
        return repository.findForUpdate(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Imóvel não encontrado."));
    }

    private void validar(ImovelModel model) {
        if (model.getTitulo() == null || model.getTitulo().isBlank()
                || model.getFinalidade() == null || model.getFinalidade().isBlank()) {
            throw invalido("Informe título e finalidade do imóvel.");
        }
        if (negativo(model.getPrecoVenda()) || negativo(model.getPrecoAluguel())
                || negativo(model.getAreaTotal()) || negativo(model.getAreaConstruida())
                || negativo(model.getDormitorios()) || negativo(model.getBanheiros()) || negativo(model.getGaragem())) {
            throw invalido("Preços, áreas e quantidades não podem ser negativos.");
        }
        if (!positivo(model.getPrecoVenda()) && !positivo(model.getPrecoAluguel())) {
            throw invalido("Informe um valor positivo para venda ou aluguel.");
        }
        if (model.getPrecoVenda() != null && model.getPrecoVenda().signum() == 0) model.setPrecoVenda(null);
        if (model.getPrecoAluguel() != null && model.getPrecoAluguel().signum() == 0) model.setPrecoAluguel(null);
        if (model.getBairro() == null || model.getBairro().getId() == null
                || !bairros.existsById(model.getBairro().getId())) {
            throw invalido("Selecione um bairro cadastrado.");
        }
        if (model.getTipoImovel() == null || model.getTipoImovel().getId() == null
                || !tipos.existsById(model.getTipoImovel().getId())) {
            throw invalido("Selecione um tipo de imóvel cadastrado.");
        }
        model.setTitulo(model.getTitulo().trim());
        model.setBairro(bairros.findById(model.getBairro().getId()).orElseThrow());
        model.setTipoImovel(tipos.findById(model.getTipoImovel().getId()).orElseThrow());
    }

    private boolean negativo(BigDecimal valor) { return valor != null && valor.signum() < 0; }
    private boolean negativo(Integer valor) { return valor != null && valor < 0; }
    private boolean positivo(BigDecimal valor) { return valor != null && valor.signum() > 0; }
    private String normalizar(String valor) {
        return valor == null ? "" : Normalizer.normalize(valor, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "").trim().toUpperCase(Locale.ROOT);
    }
    private ResponseStatusException invalido(String mensagem) { return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem); }
    private ResponseStatusException conflito(String mensagem) { return new ResponseStatusException(HttpStatus.CONFLICT, mensagem); }
}
