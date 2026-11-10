package com.example.primeiro_projeto.services;

import com.example.primeiro_projeto.models.UserModel;
import com.example.primeiro_projeto.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service //avisa o spring que aqui é o chefe de cozinha
public class UserService {
    @Autowired//avisa o spring para injetar o UserRepository automaticamente
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    //buscar metodos sql
    public List<UserModel> findAll(){
        return userRepository.findAll();
    }

    //metodo para salvar um novo usuário no banco
    public UserModel save(UserModel user){
        validar(user, null);
        user.setEmail(user.getEmail().trim().toLowerCase());
        user.setTipo(normalizarTipo(user.getTipo()));
        user.setSenha(passwordEncoder.encode(user.getSenha()));
        return userRepository.save(user);
    }

    public Optional<UserModel> update(Integer id, UserModel dados) {
        return userRepository.findById(id).map(usuario -> {
            if (dados.getNome() == null || dados.getNome().isBlank()) {
                dados.setNome(usuario.getNome());
            }
            if (dados.getEmail() == null || dados.getEmail().isBlank()) {
                dados.setEmail(usuario.getEmail());
            }
            if (dados.getTipo() == null || dados.getTipo().isBlank()) {
                dados.setTipo(usuario.getTipo());
            }
            validar(dados, id);
            usuario.setNome(dados.getNome());
            usuario.setEmail(dados.getEmail().trim().toLowerCase());
            usuario.setTipo(normalizarTipo(dados.getTipo()));
            if (dados.getSenha() != null && !dados.getSenha().isBlank()) {
                usuario.setSenha(passwordEncoder.encode(dados.getSenha()));
            }
            return userRepository.save(usuario);
        });
    }

    public boolean delete(Integer id) {
        if (!userRepository.existsById(id)) {
            return false;
        }
        userRepository.deleteById(id);
        return true;
    }

    public Optional<UserModel> autenticar(String email, String senha) {
        return userRepository.findByEmailIgnoreCase(email)
                .filter(usuario -> usuario.getSenha() != null)
                .filter(usuario -> passwordEncoder.matches(senha, usuario.getSenha()));
    }

    private void validar(UserModel user, Integer idAtual) {
        if (user == null || user.getNome() == null || user.getNome().isBlank()
                || user.getEmail() == null || !user.getEmail().contains("@")
                || user.getTipo() == null || user.getTipo().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Nome, email válido e tipo são obrigatórios.");
        }
        if (idAtual == null && (user.getSenha() == null || user.getSenha().isBlank())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A senha é obrigatória.");
        }
        userRepository.findByEmailIgnoreCase(user.getEmail().trim())
                .filter(encontrado -> !encontrado.getId().equals(idAtual))
                .ifPresent(encontrado -> {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email já cadastrado.");
                });
        normalizarTipo(user.getTipo());
    }

    private String normalizarTipo(String tipo) {
        String normalizado = tipo.trim().toUpperCase();
        if (!normalizado.equals("ADMIN") && !normalizado.equals("USER")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Tipo deve ser ADMIN ou USER.");
        }
        return normalizado;
    }
}
