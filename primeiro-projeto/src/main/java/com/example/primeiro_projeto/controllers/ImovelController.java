package com.example.primeiro_projeto.controllers;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.MailException;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.primeiro_projeto.models.ImovelModel;
import com.example.primeiro_projeto.dto.SolicitacaoInformacaoImovel;
import com.example.primeiro_projeto.services.EmailService;
import com.example.primeiro_projeto.services.ImovelService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
@RequestMapping("/imoveis")
public class ImovelController {

    public record StatusRequest(String status) {}

    private static final Logger logger = LoggerFactory.getLogger(ImovelController.class);

    @Autowired
    private ImovelService service;

    @Autowired
    private EmailService emailService;

     @GetMapping()
    public ResponseEntity<List<ImovelModel>> getAll() {
        List<ImovelModel> list = service.getAll();
        return ResponseEntity.status(HttpStatus.OK).body(list);
    }

    @GetMapping(value = "/{id}")
        public ResponseEntity<ImovelModel> find(@PathVariable Integer id) {
        ImovelModel model = service.find(id);
        if(model!=null){
            return ResponseEntity.status(HttpStatus.OK).body(model);
        }
        else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(
            @PathVariable Integer id,
            @RequestBody StatusRequest request) {
        if (request == null || request.status() == null || request.status().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "O status é obrigatório."));
        }

        ImovelModel atualizado = service.updateStatus(id, request.status());
        if (atualizado == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(atualizado);
    }

   
    @PostMapping("/sabermais/{id}")
    public ResponseEntity<String> saberMais(
            @PathVariable Integer id,
            @RequestBody SolicitacaoInformacaoImovel solicitacao) {

        if (solicitacao == null
                || textoVazio(solicitacao.nome())
                || textoVazio(solicitacao.telefone())
                || textoVazio(solicitacao.email())
                || textoVazio(solicitacao.mensagem())
                || !solicitacao.email().contains("@")) {
            return ResponseEntity.badRequest()
                    .body("Informe nome, telefone, email válido e mensagem.");
        }

 
        ImovelModel imovel = service.find(id);
        if (imovel == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Imóvel não encontrado.");
        }

       
        Map<String, Object> dadosEmail = new HashMap<>();
        dadosEmail.put("clienteNome", solicitacao.nome());
        dadosEmail.put("clienteTelefone", solicitacao.telefone());
        dadosEmail.put("clienteEmail", solicitacao.email());
        dadosEmail.put("mensagemCliente", solicitacao.mensagem());
        dadosEmail.put("mensagemImobiliaria",
                "Obrigado pelo seu interesse. Nossa equipe está à disposição para ajudar e entrará em contato em breve.");
        dadosEmail.put("imovelId", imovel.getId());
        dadosEmail.put("titulo", valorOuPadrao(imovel.getTitulo()));
        dadosEmail.put("descricao", valorOuPadrao(imovel.getDescricao()));
        dadosEmail.put("finalidade", valorOuPadrao(imovel.getFinalidade()));
        dadosEmail.put("status", valorOuPadrao(imovel.getStatus()));
        dadosEmail.put("precoVenda", valorOuPadrao(imovel.getPrecoVenda()));
        dadosEmail.put("precoAluguel", valorOuPadrao(imovel.getPrecoAluguel()));
        dadosEmail.put("dormitorios", valorOuPadrao(imovel.getDormitorios()));
        dadosEmail.put("banheiros", valorOuPadrao(imovel.getBanheiros()));
        dadosEmail.put("garagem", valorOuPadrao(imovel.getGaragem()));
        dadosEmail.put("areaTotal", valorOuPadrao(imovel.getAreaTotal()));
        dadosEmail.put("areaConstruida", valorOuPadrao(imovel.getAreaConstruida()));
        dadosEmail.put("endereco", valorOuPadrao(imovel.getEndereco()));
        dadosEmail.put("numero", valorOuPadrao(imovel.getNumero()));
        dadosEmail.put("complemento", valorOuPadrao(imovel.getComplemento()));
        dadosEmail.put("cep", valorOuPadrao(imovel.getCep()));
        dadosEmail.put("caracteristicas", valorOuPadrao(imovel.getCaracteristicas()));

        try {
           
            emailService.enviarEmailComTemplate(
                    solicitacao.email(),
                    "Informações sobre o imóvel #" + imovel.getId(),
                    "email/solicitacao-imovel",
                    dadosEmail);

            return ResponseEntity.ok("Email com as informações do imóvel enviado.");
        } catch (MessagingException | MailException e) {
            
            logger.error("Falha ao enviar email de informações do imóvel {} para {}",
                id, solicitacao.email(), e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body("Não foi possível enviar o email neste momento.");
        }
    }

    private boolean textoVazio(String valor) {
        return valor == null || valor.isBlank();
    }

    private String valorOuPadrao(Object valor) {
        return valor == null ? "Não informado" : valor.toString();
    }

    @PostMapping
    public ResponseEntity<Void> insert(@RequestBody ImovelModel model) {
        model = service.insert(model);
        URI uri =
        ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}").buildAndExpand(model.getId()).toUri();
        return ResponseEntity.created(uri).build();
        }

    @PutMapping(value = "/{id}")
    public ResponseEntity<Void> update(@RequestBody ImovelModel model, @PathVariable Integer id) {
        try {
             model.setId(id);
             model = service.update(model);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
       
    }

    @DeleteMapping(value = "/{id}")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

   
}