package com.example.primeiro_projeto.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.example.primeiro_projeto.models.FotoImovelModel;
import com.example.primeiro_projeto.models.ImovelModel;
import com.example.primeiro_projeto.repositories.FotoImovelRepository;
import com.example.primeiro_projeto.repositories.ImovelRepository;

@Service
public class FotoImovelService {

    @Autowired
    private FotoImovelRepository fotoImovelRepository;

    @Autowired
    private ImovelRepository imovelRepository;

    @Value("${app.upload-dir}")
    private String uploadDir;

    public FotoImovelModel salvar(MultipartFile arquivo, Integer imovelId) {

        // 1 -> procurar se existe algum imóvel para associar a foto
        ImovelModel imovel = imovelRepository.findById(imovelId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Imóvel não encontrado"));

        try {
            // 2 -> criar diretório de uploads se não existir
            Path pasta = Paths.get(uploadDir, String.valueOf(imovelId));

            Files.createDirectories(pasta);

            // 3 -> pegar o nome original do arquivo
            String nomeOriginal = StringUtils.cleanPath(
                    arquivo.getOriginalFilename()
            );
            String nomeFinal = UUID.randomUUID().toString() + "_" + nomeOriginal;
            System.out.println("Arquivo: " + nomeOriginal);
            System.out.println("Pasta: " + pasta);
            Path destino = pasta.resolve(nomeFinal);

            arquivo.transferTo(destino);

            // 4 -> criar o modelo e salvar o arquivo na pasta
            FotoImovelModel model = new FotoImovelModel();
            model.setCaminho(nomeFinal);
            model.setNomeArquivo(nomeFinal);
            model.setImovel(imovel);
            fotoImovelRepository.save(model);

            return model;
            
        } catch (IOException e) {
            throw new RuntimeException(
                    "Erro ao salvar a foto do imóvel: " + e.getMessage()
            );
        }
    }


    @Transactional
    public FotoImovelModel atualizar(Integer fotoId, FotoImovelModel dadosAtualizados) {
        FotoImovelModel foto = fotoImovelRepository.findById(fotoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Foto não encontrada"));

        if (dadosAtualizados.getOrdem() != null) {
            foto.setOrdem(dadosAtualizados.getOrdem());
        }

   
        if (Boolean.TRUE.equals(dadosAtualizados.getCapa())) {
            return tornarCapa(fotoId);
        }

        if (Boolean.FALSE.equals(dadosAtualizados.getCapa())) {
            foto.setCapa(false);
        }

        return fotoImovelRepository.save(foto);
    }


    @Transactional
    public FotoImovelModel tornarCapa(Integer fotoId) {
        FotoImovelModel fotoEscolhida = fotoImovelRepository.findById(fotoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Foto não encontrada"));

        Integer imovelId = fotoEscolhida.getImovel().getId();


        List<FotoImovelModel> fotosDoImovel =
                fotoImovelRepository.findByImovel_Id(imovelId);


        for (FotoImovelModel foto : fotosDoImovel) {
            foto.setCapa(foto.getId().equals(fotoId));
        }

        fotoImovelRepository.saveAll(fotosDoImovel);
        return fotoEscolhida;
    }


    @Transactional
    public void excluir(Integer fotoId) {

        FotoImovelModel foto = fotoImovelRepository.findById(fotoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Foto não encontrada"));

        Integer imovelId = foto.getImovel().getId();
        String nomeArquivo = foto.getNomeArquivo();

        Path pastaDoImovel = Paths.get(uploadDir, String.valueOf(imovelId))
                .toAbsolutePath()
                .normalize();
        Path arquivo = pastaDoImovel.resolve(nomeArquivo).normalize();

        if (!arquivo.startsWith(pastaDoImovel)) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "O caminho da foto não é válido");
        }

        try {
            Files.deleteIfExists(arquivo);
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Não foi possível excluir o arquivo da foto", e);
        }

        fotoImovelRepository.delete(foto);
    }
}