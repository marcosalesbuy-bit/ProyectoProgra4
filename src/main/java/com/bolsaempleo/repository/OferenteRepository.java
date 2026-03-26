package com.bolsaempleo.repository;

import com.bolsaempleo.model.Oferente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OferenteRepository extends JpaRepository<Oferente, Long> {

    Optional<Oferente> findByCorreoElectronico(String correoElectronico);

    Optional<Oferente> findByIdentificacion(String identificacion);

    /** Oferentes awaiting admin approval. */
    List<Oferente> findByAprobadoFalse();

    /** Approved oferentes (visible to companies for candidate search). */
    List<Oferente> findByAprobadoTrue();

    boolean existsByCorreoElectronico(String correoElectronico);

    boolean existsByIdentificacion(String identificacion);
}
