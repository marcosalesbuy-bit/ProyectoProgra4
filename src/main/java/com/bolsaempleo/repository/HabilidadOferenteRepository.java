package com.bolsaempleo.repository;

import com.bolsaempleo.model.HabilidadOferente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HabilidadOferenteRepository extends JpaRepository<HabilidadOferente, Long> {

    List<HabilidadOferente> findByOferenteId(Long oferenteId);

    Optional<HabilidadOferente> findByOferenteIdAndCaracteristicaId(Long oferenteId, Long caracteristicaId);

    void deleteByOferenteIdAndCaracteristicaId(Long oferenteId, Long caracteristicaId);
}
