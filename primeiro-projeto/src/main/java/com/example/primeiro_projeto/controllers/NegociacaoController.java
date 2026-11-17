package com.example.primeiro_projeto.controllers;

import static com.example.primeiro_projeto.dto.NegociacaoDtos.*;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.example.primeiro_projeto.services.NegociacaoService;
import com.example.primeiro_projeto.models.ImovelModel;

@RestController
public class NegociacaoController {
    private final NegociacaoService service;
    public NegociacaoController(NegociacaoService service) { this.service = service; }

    @GetMapping("/sessao")
    public Map<String, Object> sessao(Authentication auth) {
        var user = service.usuario(auth);
        return Map.of("tipo", user.getTipo(), "nome", user.getNome(), "usuarioId", user.getId());
    }
    @GetMapping("/ofertas")
    public List<Oferta> ofertas(Authentication auth, @RequestParam(required = false) String status) { return service.listarOfertas(auth, status); }
    @PostMapping("/ofertas")
    public ResponseEntity<Oferta> criarOferta(Authentication auth, @RequestBody NovaOferta dados) { return ResponseEntity.status(201).body(service.criarOferta(auth, dados)); }
    @PatchMapping("/ofertas/{id}/aceitar")
    public Oferta aceitar(Authentication auth, @PathVariable Integer id) { return service.aceitarOferta(auth, id); }
    @PatchMapping("/ofertas/{id}/recusar")
    public Oferta recusar(Authentication auth, @PathVariable Integer id) { return service.recusarOferta(auth, id); }
    @PatchMapping("/ofertas/{id}/cancelar")
    public Oferta cancelar(Authentication auth, @PathVariable Integer id) { return service.cancelarOferta(auth, id); }

    @GetMapping("/favoritos")
    public List<ImovelModel> favoritos(Authentication auth) { return service.listarFavoritos(auth); }
    @PostMapping("/favoritos/{id}")
    public ResponseEntity<Void> favoritar(Authentication auth, @PathVariable Integer id) { service.adicionarFavorito(auth, id); return ResponseEntity.status(201).build(); }
    @DeleteMapping("/favoritos/{id}")
    public ResponseEntity<Void> desfavoritar(Authentication auth, @PathVariable Integer id) { service.removerFavorito(auth, id); return ResponseEntity.noContent().build(); }

    @GetMapping("/contratos")
    public List<Contrato> contratos(Authentication auth) { return service.listarContratos(auth); }
    @PostMapping("/contratos")
    public ResponseEntity<Contrato> criarContrato(Authentication auth, @RequestBody NovoContrato dados) { return ResponseEntity.status(201).body(service.prepararContrato(auth, dados)); }
    @PatchMapping("/contratos/{id}/aprovar")
    public Contrato aprovarContrato(Authentication auth, @PathVariable Integer id) { return service.aprovarContrato(auth, id); }
    @PatchMapping("/contratos/{id}/recusar")
    public Contrato recusarContrato(Authentication auth, @PathVariable Integer id) { return service.recusarContrato(auth, id); }
    @PatchMapping("/contratos/{id}/enviar")
    public Contrato enviarContrato(Authentication auth, @PathVariable Integer id) { return service.enviarContrato(auth, id); }
    @PatchMapping("/contratos/{id}/assinar")
    public Contrato assinarContrato(Authentication auth, @PathVariable Integer id, @RequestBody Assinatura dados) { return service.assinarContrato(auth, id, dados); }
    @PatchMapping("/contratos/{id}/cancelar")
    public Contrato cancelarContrato(Authentication auth, @PathVariable Integer id) { return service.cancelarContrato(auth, id); }
    @GetMapping(value = "/contratos/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> pdf(Authentication auth, @PathVariable Integer id) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header("Content-Disposition", "attachment; filename=contrato-" + id + ".pdf").body(service.pdf(auth, id));
    }
    @GetMapping("/painel")
    public Painel painel(Authentication auth) { return service.painel(auth); }
}
