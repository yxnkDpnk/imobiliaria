package com.example.primeiro_projeto.services;

//propriedade de lista do java
import java.util.List;
//nao sei
import java.util.Optional;

//import do cabo universal spring
import org.springframework.beans.factory.annotation.Autowired;
//import do objeto Page qual acredito eu que deve agrupar uma unica pagina
import org.springframework.data.domain.Page;
//import do pageable que em tese deve ser um objetos lista com varias pages e interativo?
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
//cracha de chefe
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.primeiro_projeto.models.BairroModel;
import com.example.primeiro_projeto.repositories.BairroRepository;

@Service
public class BairroService{
    @Autowired
    private BairroRepository repository;

    //metodo que retora uma lista com todos os bairros
    public List<BairroModel> getAll(){
        List<BairroModel> list = repository.findAll();
        return list;

    }

    //aqui indicamos que as paginas seram preenchidas de bairros e por isso retornara um pageable? nao entedi pois ainda não indetifiquei o que é um objeto pageable e muito menos page
    public Page<BairroModel> getAll(Pageable pageable){
        Page<BairroModel> list = repository.findAll(pageable);
        return list;
    }

    //não sei oque significa optinonal aqui confusao total, e fiquei em duvida se pelo fato de ter <> significa que também é uma lista ou item com varias coisas
    public BairroModel find(Integer id){
        Optional<BairroModel> model = repository.findById(id);

        //primeira vez que vejo essa sintaxe mas manda um valor presetado caso nao ache nada? ou nesse caso nao consiga montar o model referido?
        return model.orElse(null);
    }



    public BairroModel insert(BairroModel model){
    
        if(repository.existsByNome(model.getNome())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Erro: Bairro já cadastrado no banco!");
            
        }
        return repository.save(model);

    }

    public BairroModel update(BairroModel model){
        try{
            //se ele achar o bairro no banco atualizara os dados e todas colunas da tabela
            if(find(model.getId()) != null){
                return repository.save(model);
            }
            return null;

        }catch(Exception e){
            return null;
        }
    }
    //autoexplicativo
    public void delete(Integer id){
        repository.deleteById(id);
    }
}