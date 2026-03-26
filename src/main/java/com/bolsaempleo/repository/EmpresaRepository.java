package com.bolsaempleo.repository;

import com.bolsaempleo.model.Empresa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

    Optional<Empresa> findByCorreoElectronico(String correoElectronico);

    /** Empresas awaiting admin approval. */
    List<Empresa> findByAprobadaFalse();

    boolean existsByCorreoElectronico(String correoElectronico);
}
