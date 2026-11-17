package com.example.primeiro_projeto.controllers;

import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.primeiro_projeto.models.UserModel;
import com.example.primeiro_projeto.services.UserService;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserModel> getAllUsers(@RequestParam(defaultValue = "") String busca) {
        String termo = busca.trim().toLowerCase(Locale.ROOT);
        return userService.findAll().stream().filter(user -> termo.isEmpty()
                || user.getNome().toLowerCase(Locale.ROOT).contains(termo)
                || user.getEmail().toLowerCase(Locale.ROOT).contains(termo)).toList();
    }

    @GetMapping("/{id}")
    public UserModel find(@PathVariable Integer id) {
        return usuario(id);
    }

    @PostMapping
    public ResponseEntity<UserModel> createUser(@RequestBody UserModel user) {
        user.setId(null);
        UserModel salvo = userService.save(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(ServletUriComponentsBuilder.fromCurrentRequest()
                        .path("/{id}").buildAndExpand(salvo.getId()).toUri())
                .body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserModel> updateUser(@PathVariable Integer id,
            @RequestBody UserModel user, Authentication authentication) {
        UserModel atual = usuario(id);
        if (atual.getEmail().equalsIgnoreCase(authentication.getName())
                && user.getTipo() != null && !"ADMIN".equalsIgnoreCase(user.getTipo().trim())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Você não pode retirar seu próprio perfil de administrador.");
        }
        return userService.update(id, user).map(ResponseEntity::ok)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id, Authentication authentication) {
        if (usuario(id).getEmail().equalsIgnoreCase(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Um administrador não pode excluir a si mesmo.");
        }
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private UserModel usuario(Integer id) {
        return userService.findAll().stream().filter(user -> user.getId().equals(id)).findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));
    }
}
