package com.example.primeiro_projeto.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.example.primeiro_projeto.models.UserModel;
import com.example.primeiro_projeto.repositories.UserRepository;

@Configuration
public class DadosIniciaisConfig {
    @Bean
    CommandLineRunner prepararUsuariosDaAula(UserRepository repository, PasswordEncoder encoder) {
        return args -> {
            criarSeAusente(repository, encoder, "Administrador", "admin@imobiliaria.com", "ADMIN");
            criarSeAusente(repository, encoder, "Cliente", "cliente@imobiliaria.com", "USER");
            criarSeAusente(repository, encoder, "Corretor", "corretor@imobiliaria.com", "CORRETOR");
        };
    }
    private void criarSeAusente(UserRepository repository, PasswordEncoder encoder, String nome, String email, String tipo) {
        if (repository.findByEmailIgnoreCase(email).isPresent()) return;
        UserModel usuario = new UserModel(); usuario.setNome(nome); usuario.setEmail(email); usuario.setTipo(tipo);
        usuario.setSenha(encoder.encode("1234")); repository.save(usuario);
    }
}
