package com.example.primeiro_projeto.repositories;
import com.example.primeiro_projeto.models.ContratoModel;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface ContratoRepository extends JpaRepository<ContratoModel, Integer> {
    boolean existsByOferta_Id(Integer ofertaId);
    List<ContratoModel> findAllByOrderByIdDesc();
    List<ContratoModel> findByOferta_Usuario_IdOrderByIdDesc(Integer usuarioId);
}
