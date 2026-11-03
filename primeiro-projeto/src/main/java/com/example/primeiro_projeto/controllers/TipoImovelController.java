package com.example.primeiro_projeto.controllers;

import com.example.primeiro_projeto.models.TipoImovelModel;
import com.example.primeiro_projeto.services.TipoImovelService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@RestController
@RequestMapping("/tipos-imovel")
public class TipoImovelController {

    @Autowired
    private TipoImovelService tipoImovelService;

    @GetMapping
    public List<TipoImovelModel> getAll(){
        return tipoImovelService.getAll();
    }

    @GetMapping("/tipos-imovel-page")
    public Page<TipoImovelModel> getAll(Pageable pageable){
        return tipoImovelService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public TipoImovelModel find(@PathVariable Integer id){
        return tipoImovelService.find(id);
    }

    @PostMapping
    public TipoImovelModel insert(@RequestBody TipoImovelModel model){
        return tipoImovelService.insert(model);
    }

    @PutMapping
    public TipoImovelModel update(@RequestBody TipoImovelModel model){
        return tipoImovelService.update(model);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id){
        tipoImovelService.delete(id);
    }
}