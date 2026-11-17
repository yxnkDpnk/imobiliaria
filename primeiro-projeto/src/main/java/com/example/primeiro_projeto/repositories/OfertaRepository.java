package com.example.primeiro_projeto.repositories;
import com.example.primeiro_projeto.models.OfertaModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface OfertaRepository extends JpaRepository<OfertaModel, Integer> {
    List<OfertaModel> findByImovel_IdOrderById(Integer imovelId);
    List<OfertaModel> findByUsuario_IdOrderByIdDesc(Integer usuarioId);
    List<OfertaModel> findAllByOrderByIdDesc();
}
