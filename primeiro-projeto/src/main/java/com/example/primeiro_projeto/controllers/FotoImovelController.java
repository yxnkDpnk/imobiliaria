package com.example.primeiro_projeto.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.primeiro_projeto.models.FotoImovelModel;
import com.example.primeiro_projeto.services.FotoImovelService;

@RestController
@RequestMapping("/fotos")
public class FotoImovelController {

    private static final Logger logger = LoggerFactory.getLogger(FotoImovelController.class);

    private final FotoImovelService fotoImovelService;

    public FotoImovelController(FotoImovelService fotoImovelService) {
        this.fotoImovelService = fotoImovelService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FotoImovelModel> salvar(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("imovelId") Integer imovelId) {
        try {
            if(arquivo.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }

            FotoImovelModel fotoImovelModel = fotoImovelService.salvar(arquivo, imovelId);
            return ResponseEntity.status(HttpStatus.CREATED).body(fotoImovelModel);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            logger.error("Erro inesperado ao salvar foto do imóvel {}", imovelId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<FotoImovelModel> atualizar(
            @PathVariable Integer id,
            @RequestBody FotoImovelModel dadosAtualizados) {
        try {
            FotoImovelModel fotoAtualizada =
                    fotoImovelService.atualizar(id, dadosAtualizados);
            return ResponseEntity.ok(fotoAtualizada);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/{id}/capa")
    public ResponseEntity<FotoImovelModel> tornarCapa(@PathVariable Integer id) {
        try {
            FotoImovelModel fotoCapa = fotoImovelService.tornarCapa(id);
            return ResponseEntity.ok(fotoCapa);
        } catch (ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        try {
            fotoImovelService.excluir(id);
            return ResponseEntity.noContent().build();
        } catch (ResponseStatusException e) {
    
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
