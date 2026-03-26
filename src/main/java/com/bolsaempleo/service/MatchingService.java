package com.bolsaempleo.service;

import com.bolsaempleo.dto.MatchingResultDTO;
import com.bolsaempleo.model.HabilidadOferente;
import com.bolsaempleo.model.Oferente;
import com.bolsaempleo.model.Puesto;
import com.bolsaempleo.model.RequisitoPuesto;
import com.bolsaempleo.repository.HabilidadOferenteRepository;
import com.bolsaempleo.repository.OferenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Core business logic: match a puesto's requirements against all approved
 * oferentes and rank them by percentage of requirements met.
 *
 * Algorithm:
 *   For each approved oferente, check every RequisitoPuesto.
 *   A requirement is satisfied when:
 *     - the oferente has a HabilidadOferente for the same caracteristica, AND
 *     - the habilidad.nivel >= requisito.nivelRequerido
 *
 *   % coincidencia = (requirements satisfied / total requirements) * 100
 *
 * Only oferentes with at least one satisfied requirement are returned,
 * sorted descending by % coincidencia.
 */
@Service
@RequiredArgsConstructor
public class MatchingService {

    private final OferenteRepository oferenteRepository;
    private final HabilidadOferenteRepository habilidadRepository;

    /**
     * Find and rank candidates for the given puesto.
     *
     * @param puesto the job position with its requisitos pre-loaded
     * @return sorted list of matching results (best match first)
     */
    public List<MatchingResultDTO> buscarCandidatos(Puesto puesto) {
        List<RequisitoPuesto> requisitos = puesto.getRequisitos();
        int totalRequisitos = requisitos.size();

        List<Oferente> aprobados = oferenteRepository.findByAprobadoTrue();

        return aprobados.stream()
                .map(oferente -> calcularMatch(oferente, requisitos, totalRequisitos))
                .filter(result -> result.getRequisitosCumplidos() > 0)
                .sorted(Comparator.comparingDouble(MatchingResultDTO::getPorcentajeCoincidencia).reversed())
                .collect(Collectors.toList());
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private MatchingResultDTO calcularMatch(Oferente oferente,
                                            List<RequisitoPuesto> requisitos,
                                            int totalRequisitos) {

        // Build a map of caracteristicaId → nivel for this oferente
        List<HabilidadOferente> habilidades = habilidadRepository.findByOferenteId(oferente.getId());
        Map<Long, Integer> nivelPorCaracteristica = habilidades.stream()
                .collect(Collectors.toMap(
                        h -> h.getCaracteristica().getId(),
                        HabilidadOferente::getNivel
                ));

        // Count how many requirements the oferente satisfies
        int cumplidos = 0;
        for (RequisitoPuesto req : requisitos) {
            Long cid = req.getCaracteristica().getId();
            Integer nivelOferente = nivelPorCaracteristica.get(cid);
            if (nivelOferente != null && nivelOferente >= req.getNivelRequerido()) {
                cumplidos++;
            }
        }

        return new MatchingResultDTO(oferente, cumplidos, totalRequisitos);
    }
}
