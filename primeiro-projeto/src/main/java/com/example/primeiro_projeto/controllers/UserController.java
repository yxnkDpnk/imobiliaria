
package com.example.primeiro_projeto.controllers;

import com.example.primeiro_projeto.models.UserModel;
import com.example.primeiro_projeto.services.UserService;

// Imports do Spring Web (Rotas e Navegador)

//mapeador para gets
import org.springframework.web.bind.annotation.GetMapping;

//mapeador para post
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;

import org.springframework.web.bind.annotation.RequestBody;

//Indica com quais clientes (com/url) o garçom vai lidar
import org.springframework.web.bind.annotation.RequestMapping;

//cracha de garçom
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

//import da injeção de dependencias (O cabo universal conector de classes)
import org.springframework.beans.factory.annotation.Autowired;


//importar lista do java
import java.util.List;

@RestController //Sou um garçom
@RequestMapping("/users")
public class UserController{
    //pluga o chef de cozinha (UserService) no Garçom (eu)
    @Autowired
    private UserService userService; 

    //chamou get nesse RequestMapping? Então vamos puxar todos usuarios
    @GetMapping
    public List<UserModel> getAllUsers(){
        return userService.findAll();
    }

    //Enviou post nesse RequestMapping? Então vamos salvar um usuário novo
    @PostMapping
    public UserModel createUser(@RequestBody UserModel user){
        return userService.save(user);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserModel> updateUser(
            @PathVariable Integer id,
            @RequestBody UserModel user) {
        return userService.update(id, user)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer id) {
        if (!userService.delete(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.noContent().build();
    }
}
