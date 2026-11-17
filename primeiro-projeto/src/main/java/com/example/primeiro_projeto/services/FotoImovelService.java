package com.example.primeiro_projeto.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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
        if (arquivo == null || arquivo.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Selecione uma imagem PNG, JPEG ou GIF.");
        }
        ImovelModel imovel = imovelRepository.findById(imovelId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "Imóvel não encontrado"));

        try {
            String formato;
            try (ImageInputStream imagem = ImageIO.createImageInputStream(arquivo.getInputStream())) {
                var leitores = ImageIO.getImageReaders(imagem);
                if (!leitores.hasNext()) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "O arquivo não é uma imagem PNG, JPEG ou GIF válida.");
                }
                ImageReader leitor = leitores.next();
                try {
                    formato = leitor.getFormatName().toLowerCase(java.util.Locale.ROOT);
                    if (!List.of("png", "jpeg", "jpg", "gif").contains(formato)) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Use uma imagem PNG, JPEG ou GIF.");
                    }
                    leitor.setInput(imagem);
                    if ((long) leitor.getWidth(0) * leitor.getHeight(0) > 40_000_000) {
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A imagem deve ter no máximo 40 megapixels.");
                    }
                    leitor.read(0);
                } finally { leitor.dispose(); }
            }
            Path pasta = Paths.get(uploadDir, String.valueOf(imovelId)).toAbsolutePath().normalize();
            Files.createDirectories(pasta);
            String nomeFinal = UUID.randomUUID() + "." + formato;
            Path destino = pasta.resolve(nomeFinal);
            arquivo.transferTo(destino);
            FotoImovelModel model = new FotoImovelModel();
            model.setCaminho(nomeFinal);
            model.setNomeArquivo(nomeFinal);
            model.setImovel(imovel);
            List<FotoImovelModel> existentes = fotoImovelRepository.findByImovel_Id(imovelId);
            model.setCapa(existentes.isEmpty());
            model.setOrdem(existentes.size() + 1);
            try {
                return fotoImovelRepository.saveAndFlush(model);
            } catch (RuntimeException e) {
                Files.deleteIfExists(destino);
                throw e;
            }
            
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Não foi possível ler ou salvar a imagem. Use um arquivo PNG, JPEG ou GIF válido.", e);
        }
    }


    @Transactional
    public FotoImovelModel atualizar(Integer fotoId, FotoImovelModel dadosAtualizados) {
        FotoImovelModel foto = fotoImovelRepository.findById(fotoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Foto não encontrada"));

        if (dadosAtualizados.getOrdem() != null) {
            if (dadosAtualizados.getOrdem() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A ordem da foto não pode ser negativa.");
            }
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
