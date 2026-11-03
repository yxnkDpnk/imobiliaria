package com.example.primeiro_projeto.controllers;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.primeiro_projeto.security.JwtService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final JwtService jwtService;

    // Spring fornece automaticamente o JwtService
    public AuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public record LoginRequest(String login, String senha) {}

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest dados) {

        if (dados == null || dados.login() == null || dados.senha() == null) {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Login ou senha incorretos"));
        }

        String perfil;

        if ("admin".equals(dados.login()) &&
            "1234".equals(dados.senha())) {

            perfil = "ADMIN";

        } else if ("usuario".equals(dados.login()) &&
                   "1234".equals(dados.senha())) {

            perfil = "USER";

        } else {
            return ResponseEntity.status(401)
                .body(Map.of("erro", "Login ou senha incorretos"));
        }

        // Credenciais corretas: gerar o JWT
        String token = jwtService.gerarToken(
            dados.login(),
            perfil
        );

        return ResponseEntity.ok(Map.of(
            "token", token,
            "tipo", "Bearer"
        ));
    }
}