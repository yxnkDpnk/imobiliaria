package com.example.primeiro_projeto.services;

import java.util.List;
import java.util.Locale;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.primeiro_projeto.models.BairroModel;
import com.example.primeiro_projeto.repositories.BairroRepository;

@Service
public class BairroService {
    private final BairroRepository repository;

    public BairroService(BairroRepository repository) {
        this.repository = repository;
    }

    public List<BairroModel> getAll() { return repository.findAll(); }
    public Page<BairroModel> getAll(Pageable pageable) { return repository.findAll(pageable); }

    public BairroModel find(Integer id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Bairro não encontrado."));
    }

    @Transactional
    public BairroModel insert(BairroModel model) {
        model.setId(null);
        validar(model);
        return repository.save(model);
    }

    @Transactional
    public BairroModel update(BairroModel model) {
        if (model.getId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o código do bairro.");
        BairroModel atual = find(model.getId());
        if (model.getNome() != null) atual.setNome(model.getNome());
        if (model.getCidade() != null) atual.setCidade(model.getCidade());
        if (model.getEstado() != null) atual.setEstado(model.getEstado());
        if (model.getCepInicial() != null) atual.setCepInicial(model.getCepInicial());
        if (model.getCepFinal() != null) atual.setCepFinal(model.getCepFinal());
        validar(atual);
        return repository.save(atual);
    }

    @Transactional
    public void delete(Integer id) { repository.delete(find(id)); repository.flush(); }

    private void validar(BairroModel model) {
        if (model.getNome() == null || model.getNome().isBlank()
                || model.getCidade() == null || model.getCidade().isBlank()
                || model.getEstado() == null || !model.getEstado().trim().matches("[A-Za-z]{2}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe nome, cidade e a UF com duas letras.");
        }
        model.setNome(model.getNome().trim());
        model.setCidade(model.getCidade().trim());
        model.setEstado(model.getEstado().trim().toUpperCase(Locale.ROOT));
        model.setCepInicial(model.getCepInicial() == null ? "" : model.getCepInicial().trim());
        model.setCepFinal(model.getCepFinal() == null ? "" : model.getCepFinal().trim());
        boolean duplicado = repository.findAll().stream().anyMatch(outro ->
                !outro.getId().equals(model.getId()) && outro.getNome().equalsIgnoreCase(model.getNome())
                && outro.getCidade().equalsIgnoreCase(model.getCidade())
                && outro.getEstado().equalsIgnoreCase(model.getEstado()));
        if (duplicado) throw new ResponseStatusException(HttpStatus.CONFLICT, "Este bairro já está cadastrado nesta cidade.");
    }
}
