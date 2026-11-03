package com.example.primeiro_projeto.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.primeiro_projeto.models.TipoImovelModel;
import com.example.primeiro_projeto.repositories.TipoImovelRepository;

@Service
public class TipoImovelService {

    @Autowired
    private TipoImovelRepository repository;

    // retorna uma lista com todos os tipos de imovel
    public List<TipoImovelModel> getAll(){
        List<TipoImovelModel> list = repository.findAll();
        return list;
    }


    public Page<TipoImovelModel> getAll(Pageable pageable){
        Page<TipoImovelModel> list = repository.findAll(pageable);
        return list;
    }

    public TipoImovelModel find(Integer id){
        Optional<TipoImovelModel> model = repository.findById(id);
        return model.orElse(null);
    }

    public TipoImovelModel insert(TipoImovelModel model){
        if(repository.existsByNome(model.getNome())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro: Tipo de Imóvel já cadastrado no banco!");
        }
        return repository.save(model);
    }

    public TipoImovelModel update(TipoImovelModel model){
        try{
            if(find(model.getId()) != null){
                return repository.save(model);
            }
            return null;

        }catch(Exception e){
            return null;
        }
    }

    public void delete(Integer id){
        repository.deleteById(id);
    }
}