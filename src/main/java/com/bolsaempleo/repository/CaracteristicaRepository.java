package com.bolsaempleo.repository;

import com.bolsaempleo.model.Caracteristica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CaracteristicaRepository extends JpaRepository<Caracteristica, Long> {

    /** Root categories (no parent). */
    List<Caracteristica> findByPadreIsNullOrderByNombreAsc();

    /** Direct children of a given parent. */
    List<Caracteristica> findByPadreIdOrderByNombreAsc(Long padreId);

    /** All characteristics ordered alphabetically (for dropdowns). */
    List<Caracteristica> findAllByOrderByNombreAsc();

    /** Leaf nodes (characteristics with no children) — used in skill selection. */
    @Query("SELECT c FROM Caracteristica c WHERE c.id NOT IN " +
           "(SELECT DISTINCT child.padre.id FROM Caracteristica child WHERE child.padre IS NOT NULL)")
    List<Caracteristica> findHojas();
}
