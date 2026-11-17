package com.example.primeiro_projeto.services;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.example.primeiro_projeto.models.TipoImovelModel;
import com.example.primeiro_projeto.repositories.TipoImovelRepository;

@Service
public class TipoImovelService {
    private final TipoImovelRepository repository;
    public TipoImovelService(TipoImovelRepository repository) { this.repository = repository; }
    public List<TipoImovelModel> getAll() { return repository.findAll(); }
    public Page<TipoImovelModel> getAll(Pageable pageable) { return repository.findAll(pageable); }
    public TipoImovelModel find(Integer id) {
        return repository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de imóvel não encontrado."));
    }
    @Transactional
    public TipoImovelModel insert(TipoImovelModel model) {
        model.setId(null);
        validar(model);
        return repository.save(model);
    }
    @Transactional
    public TipoImovelModel update(TipoImovelModel model) {
        if (model.getId() == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o código do tipo de imóvel.");
        TipoImovelModel atual = find(model.getId());
        if (model.getNome() != null) atual.setNome(model.getNome());
        if (model.getDescricao() != null) atual.setDescricao(model.getDescricao());
        validar(atual);
        return repository.save(atual);
    }
    @Transactional
    public void delete(Integer id) { repository.delete(find(id)); repository.flush(); }
    private void validar(TipoImovelModel model) {
        if (model.getNome() == null || model.getNome().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Informe o nome do tipo de imóvel.");
        }
        model.setNome(model.getNome().trim());
        if (repository.findAll().stream().anyMatch(outro -> !outro.getId().equals(model.getId())
                && outro.getNome().equalsIgnoreCase(model.getNome()))) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Este tipo de imóvel já está cadastrado.");
        }
    }
}
