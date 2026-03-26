package com.bolsaempleo.service;

import com.bolsaempleo.model.Caracteristica;
import com.bolsaempleo.model.Empresa;
import com.bolsaempleo.model.Puesto;
import com.bolsaempleo.model.RequisitoPuesto;
import com.bolsaempleo.repository.CaracteristicaRepository;
import com.bolsaempleo.repository.PuestoRepository;
import com.bolsaempleo.repository.RequisitoPuestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PuestoService {

    private final PuestoRepository puestoRepository;
    private final RequisitoPuestoRepository requisitoPuestoRepository;
    private final CaracteristicaRepository caracteristicaRepository;

    // ── Public home page ──────────────────────────────────────────────────────

    /** 5 most recent active public puestos for the public home page. */
    public List<Puesto> obtenerUltimosPublicos() {
        return puestoRepository.findTop5ByEsPublicoTrueAndActivoTrueOrderByFechaRegistroDesc();
    }

    // ── Public / oferente search ──────────────────────────────────────────────

    /** Public search (unauthenticated visitors). */
    public List<Puesto> buscarPublicos(List<Long> caracteristicaIds) {
        if (caracteristicaIds == null || caracteristicaIds.isEmpty()) {
            return puestoRepository.findByEsPublicoTrueAndActivoTrue();
        }
        return puestoRepository.findPublicosActivosByCaracteristicas(caracteristicaIds);
    }

    /** Search visible to authenticated oferentes (includes private puestos). */
    public List<Puesto> buscarParaOferente(List<Long> caracteristicaIds) {
        if (caracteristicaIds == null || caracteristicaIds.isEmpty()) {
            return puestoRepository.findByActivoTrue();
        }
        return puestoRepository.findActivosByCaracteristicas(caracteristicaIds);
    }

    // ── Company operations ────────────────────────────────────────────────────

    /** All puestos belonging to a company, newest first. */
    public List<Puesto> obtenerPorEmpresa(Long empresaId) {
        return puestoRepository.findByEmpresaIdOrderByFechaRegistroDesc(empresaId);
    }

    public Optional<Puesto> findById(Long id) {
        return puestoRepository.findById(id);
    }

    /**
     * Publish a new puesto with its requirements.
     *
     * @param empresa          owning company
     * @param descripcion      job description
     * @param salarioOfrecido  offered salary
     * @param esPublico        visibility flag
     * @param caracteristicaIds  list of characteristic IDs required
     * @param nivelesRequeridos  parallel list of minimum levels (1–5)
     */
    @Transactional
    public Puesto publicar(Empresa empresa,
                           String descripcion,
                           BigDecimal salarioOfrecido,
                           boolean esPublico,
                           List<Long> caracteristicaIds,
                           List<Integer> nivelesRequeridos) {

        Puesto puesto = new Puesto();
        puesto.setEmpresa(empresa);
        puesto.setDescripcion(descripcion);
        puesto.setSalarioOfrecido(salarioOfrecido);
        puesto.setEsPublico(esPublico);
        puesto.setActivo(true);
        puesto = puestoRepository.save(puesto);

        agregarRequisitos(puesto, caracteristicaIds, nivelesRequeridos);
        return puesto;
    }

    /**
     * Update an existing puesto.
     */
    @Transactional
    public Puesto actualizar(Long puestoId,
                             String descripcion,
                             BigDecimal salarioOfrecido,
                             boolean esPublico,
                             List<Long> caracteristicaIds,
                             List<Integer> nivelesRequeridos) {

        Puesto puesto = puestoRepository.findById(puestoId)
                .orElseThrow(() -> new IllegalArgumentException("Puesto no encontrado: " + puestoId));

        puesto.setDescripcion(descripcion);
        puesto.setSalarioOfrecido(salarioOfrecido);
        puesto.setEsPublico(esPublico);

        // Replace all requirements
        requisitoPuestoRepository.deleteByPuestoId(puestoId);
        puesto.getRequisitos().clear();
        puestoRepository.save(puesto);

        agregarRequisitos(puesto, caracteristicaIds, nivelesRequeridos);
        return puesto;
    }

    /** Deactivate a puesto (soft-delete). */
    @Transactional
    public void desactivar(Long puestoId) {
        Puesto puesto = puestoRepository.findById(puestoId)
                .orElseThrow(() -> new IllegalArgumentException("Puesto no encontrado: " + puestoId));
        puesto.setActivo(false);
        puestoRepository.save(puesto);
    }

    // ── Admin report ──────────────────────────────────────────────────────────

    /** Puestos created in a specific year/month (for the admin PDF report). */
    public List<Puesto> obtenerPorMes(int anio, int mes) {
        LocalDateTime inicio = LocalDateTime.of(anio, mes, 1, 0, 0);
        LocalDateTime fin = inicio.plusMonths(1);
        return puestoRepository.findByMes(inicio, fin);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private void agregarRequisitos(Puesto puesto, List<Long> caracteristicaIds, List<Integer> niveles) {
        if (caracteristicaIds == null) return;
        for (int i = 0; i < caracteristicaIds.size(); i++) {
            Long cid = caracteristicaIds.get(i);
            if (cid == null) continue;
            int nivel = (niveles != null && i < niveles.size() && niveles.get(i) != null)
                    ? niveles.get(i) : 1;

            Caracteristica c = caracteristicaRepository.findById(cid)
                    .orElseThrow(() -> new IllegalArgumentException("Característica no encontrada: " + cid));

            RequisitoPuesto req = new RequisitoPuesto();
            req.setPuesto(puesto);
            req.setCaracteristica(c);
            req.setNivelRequerido(nivel);
            requisitoPuestoRepository.save(req);
        }
    }
}
