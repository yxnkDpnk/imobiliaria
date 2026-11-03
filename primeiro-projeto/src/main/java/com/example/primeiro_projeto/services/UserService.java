package com.example.primeiro_projeto.services;

import com.example.primeiro_projeto.models.UserModel;
import com.example.primeiro_projeto.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service //avisa o spring que aqui é o chefe de cozinha
public class UserService {
    @Autowired//avisa o spring para injetar o UserRepository automaticamente
    private UserRepository userRepository;

    //buscar metodos sql
    public List<UserModel> findAll(){
        return userRepository.findAll();
    }

    //metodo para salvar um novo usuário no banco
    public UserModel save(UserModel user){
        return userRepository.save(user);
    }

    public Optional<UserModel> update(Integer id, UserModel dados) {
        return userRepository.findById(id).map(usuario -> {
            usuario.setNome(dados.getNome());
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
}