package com.example.primeiro_projeto.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.primeiro_projeto.security.JwtService;
import com.example.primeiro_projeto.models.UserModel;
import com.example.primeiro_projeto.services.UserService;

@RestController
public class AuthController {

    private final JwtService jwtService;
    private final UserService userService;

    // Spring fornece automaticamente o JwtService
    public AuthController(JwtService jwtService, UserService userService) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    public record LoginRequest(String email, String login, String senha) {}

    @PostMapping({"/public/login", "/auth/login"})
    public ResponseEntity<?> login(@RequestBody LoginRequest dados) {

        if (dados == null || dados.senha() == null || dados.senha().isBlank()) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Email ou senha incorretos"));
        }

        String identificador = dados.email() != null ? dados.email() : dados.login();
        if (identificador == null || identificador.isBlank()) {
            return ResponseEntity.status(401)
                    .body(Map.of("erro", "Email ou senha incorretos"));
        }

        if ("admin".equalsIgnoreCase(identificador)) identificador = "admin@imobiliaria.com";
        if ("usuario".equalsIgnoreCase(identificador)) identificador = "cliente@imobiliaria.com";

        UserModel usuario = userService.autenticar(identificador.trim(), dados.senha())
                .orElse(null);
        if (usuario == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("erro", "Email ou senha incorretos"));
        }

        String token = jwtService.gerarToken(usuario.getEmail(), usuario.getTipo());

        return ResponseEntity.ok(Map.of(
            "token", token,
            "tipo", usuario.getTipo(),
            "nome", usuario.getNome(),
            "usuarioId", usuario.getId()
        ));
    }
}
