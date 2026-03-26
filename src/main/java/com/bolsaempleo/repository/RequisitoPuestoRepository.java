package com.bolsaempleo.repository;

import com.bolsaempleo.model.RequisitoPuesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RequisitoPuestoRepository extends JpaRepository<RequisitoPuesto, Long> {

    List<RequisitoPuesto> findByPuestoId(Long puestoId);

    void deleteByPuestoId(Long puestoId);
}
