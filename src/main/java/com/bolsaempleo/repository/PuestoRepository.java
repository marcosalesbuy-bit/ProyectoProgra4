package com.bolsaempleo.repository;

import com.bolsaempleo.model.Puesto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PuestoRepository extends JpaRepository<Puesto, Long> {

    /** Last 5 active public puestos — shown on the public home page. */
    List<Puesto> findTop5ByEsPublicoTrueAndActivoTrueOrderByFechaRegistroDesc();

    /** All active public puestos (for public search). */
    List<Puesto> findByEsPublicoTrueAndActivoTrue();

    /** All active puestos visible to an authenticated oferente (public + private). */
    List<Puesto> findByActivoTrue();

    /** All puestos belonging to a company. */
    List<Puesto> findByEmpresaIdOrderByFechaRegistroDesc(Long empresaId);

    /**
     * Public active puestos that require at least one of the given caracteristica IDs.
     * Used for public search.
     */
    @Query("SELECT DISTINCT p FROM Puesto p JOIN p.requisitos r " +
           "WHERE p.esPublico = true AND p.activo = true " +
           "AND r.caracteristica.id IN :ids")
    List<Puesto> findPublicosActivosByCaracteristicas(@Param("ids") List<Long> ids);

    /**
     * Active puestos (public + private) containing at least one of the given
     * caracteristica IDs. Used for authenticated oferente search.
     */
    @Query("SELECT DISTINCT p FROM Puesto p JOIN p.requisitos r " +
           "WHERE p.activo = true AND r.caracteristica.id IN :ids")
    List<Puesto> findActivosByCaracteristicas(@Param("ids") List<Long> ids);

    /**
     * Puestos created within a month window — used for the admin PDF report.
     */
    @Query("SELECT p FROM Puesto p WHERE p.fechaRegistro >= :inicio AND p.fechaRegistro < :fin " +
           "ORDER BY p.fechaRegistro ASC")
    List<Puesto> findByMes(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);
}
