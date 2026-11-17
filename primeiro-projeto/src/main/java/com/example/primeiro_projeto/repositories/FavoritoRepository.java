package com.example.primeiro_projeto.repositories;
import com.example.primeiro_projeto.models.FavoritoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
public interface FavoritoRepository extends JpaRepository<FavoritoModel, Integer> {
    List<FavoritoModel> findByUsuario_IdOrderByIdDesc(Integer usuarioId);
    Optional<FavoritoModel> findByUsuario_IdAndImovel_Id(Integer usuarioId, Integer imovelId);
}
