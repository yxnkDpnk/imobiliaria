
package com.example.primeiro_projeto.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.primeiro_projeto.services.EmailService;

@RestController
@RequestMapping("/email")
public class EmailController {

    private final EmailService emailService;

    public EmailController(EmailService emailService) {
        this.emailService = emailService;
    }

    @GetMapping("/template")
    public ResponseEntity<String> enviarTemplate() {
        try {
            emailService.enviarEmailComTemplate(
                "douglasmartinserdmann2@gmail.com",
                "Bem-vindo!",
                "email/template",
                Map.of(
                    "nome", "Maria",
                    "itens", List.of("Plano Pro", "Suporte 24h"),
                    "link", "https://meusite.com"
                )
            );

            return ResponseEntity.ok("Email enviado com sucesso!");

        } catch (Exception e) {
            e.printStackTrace();

            return ResponseEntity.status(500)
                .body("Erro ao enviar email: " + e.getMessage());
        }
    }
}
