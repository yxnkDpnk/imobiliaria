package com.example.primeiro_projeto.controllers;

import com.example.primeiro_projeto.models.BairroModel;
import com.example.primeiro_projeto.services.BairroService;

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
@RequestMapping("/bairros")
public class BairroController {

    @Autowired
    private BairroService bairroService;

    @GetMapping
    public List<BairroModel> getAll(){
        return bairroService.getAll();
    }

    @GetMapping("/bairros-page")
    public Page<BairroModel> getAll(Pageable pageable){
        return bairroService.getAll(pageable);
    }

    @GetMapping("/{id}")
    public BairroModel find(@PathVariable Integer id){
        return bairroService.find(id);
    }

    @PostMapping
    public BairroModel insert(@RequestBody BairroModel model){
        return bairroService.insert(model);
    }

    @PutMapping
    public BairroModel update(@RequestBody BairroModel model){
        return bairroService.update(model);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Integer id){
        bairroService.delete(id);
    }
}