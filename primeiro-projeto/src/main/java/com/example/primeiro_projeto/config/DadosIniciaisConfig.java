package com.example.primeiro_projeto.config;

import java.util.List;

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
            List<UserModel> usuarios = repository.findAll();

            UserModel admin = usuarios.stream()
                    .filter(usuario -> usuario.getNome() != null
                            && usuario.getNome().toLowerCase().contains("admin"))
                    .findFirst()
                    .orElseGet(UserModel::new);
            preencher(admin, "Administrador", "admin@imobiliaria.com", "ADMIN", encoder);
            repository.save(admin);

            UserModel cliente = usuarios.stream()
                    .filter(usuario -> admin.getId() == null || !usuario.getId().equals(admin.getId()))
                    .findFirst()
                    .orElseGet(UserModel::new);
            preencher(cliente, cliente.getNome() == null ? "Cliente" : cliente.getNome(),
                    "cliente@imobiliaria.com", "USER", encoder);
            repository.save(cliente);

            for (UserModel usuario : repository.findAll()) {
                if (usuario.getEmail() == null || usuario.getEmail().isBlank()) {
                    usuario.setEmail("usuario" + usuario.getId() + "@imobiliaria.com");
                }
                if (usuario.getTipo() == null || usuario.getTipo().isBlank()) usuario.setTipo("USER");
                if (usuario.getSenha() == null || usuario.getSenha().isBlank()) {
                    usuario.setSenha(encoder.encode("1234"));
                }
                repository.save(usuario);
            }
        };
    }

    private void preencher(UserModel usuario, String nome, String email, String tipo,
            PasswordEncoder encoder) {
        usuario.setNome(nome);
        usuario.setEmail(email);
        usuario.setTipo(tipo);
        if (usuario.getSenha() == null || !usuario.getSenha().startsWith("$2")) {
            usuario.setSenha(encoder.encode("1234"));
        }
    }
}
