package com.example.primeiro_projeto.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.primeiro_projeto.models.FotoImovelModel;
import com.example.primeiro_projeto.services.FotoImovelService;
import com.example.primeiro_projeto.repositories.FotoImovelRepository;
import java.util.List;

@RestController
@RequestMapping("/fotos")
public class FotoImovelController {

    private final FotoImovelService fotoImovelService;
    private final FotoImovelRepository fotoImovelRepository;

    public FotoImovelController(FotoImovelService fotoImovelService,
            FotoImovelRepository fotoImovelRepository) {
        this.fotoImovelService = fotoImovelService;
        this.fotoImovelRepository = fotoImovelRepository;
    }

    public record FotoPublica(Integer id, String nomeArquivo, Boolean capa,
            Integer ordem, String url) {}

    @GetMapping("/imovel/{imovelId}")
    public List<FotoPublica> listar(@PathVariable Integer imovelId) {
        return fotoImovelRepository.findByImovel_Id(imovelId).stream()
                .map(foto -> new FotoPublica(foto.getId(), foto.getNomeArquivo(),
                        foto.getCapa(), foto.getOrdem(),
                        "/fotos/arquivo/" + imovelId + "/" + foto.getNomeArquivo()))
                .sorted((a, b) -> Integer.compare(
                        a.ordem() == null ? Integer.MAX_VALUE : a.ordem(),
                        b.ordem() == null ? Integer.MAX_VALUE : b.ordem()))
                .toList();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<FotoImovelModel> salvar(
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam("imovelId") Integer imovelId) {
        FotoImovelModel fotoImovelModel = fotoImovelService.salvar(arquivo, imovelId);
        return ResponseEntity.status(HttpStatus.CREATED).body(fotoImovelModel);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FotoImovelModel> atualizar(
            @PathVariable Integer id,
            @RequestBody FotoImovelModel dadosAtualizados) {
        return ResponseEntity.ok(fotoImovelService.atualizar(id, dadosAtualizados));
    }

    @PutMapping("/{id}/capa")
    public ResponseEntity<FotoImovelModel> tornarCapa(@PathVariable Integer id) {
        return ResponseEntity.ok(fotoImovelService.tornarCapa(id));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        fotoImovelService.excluir(id);
        return ResponseEntity.noContent().build();
    }
}
