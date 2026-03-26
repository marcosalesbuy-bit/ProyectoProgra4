package com.bolsaempleo.dto;

import com.bolsaempleo.model.Oferente;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Result of the skill-matching algorithm for a single oferente against a puesto.
 */
@Getter
@AllArgsConstructor
public class MatchingResultDTO {

    private final Oferente oferente;

    /** How many of the puesto's requirements this oferente satisfies. */
    private final int requisitosCumplidos;

    /** Total requirements the puesto has. */
    private final int totalRequisitos;

    /** Percentage of requirements met (0.0–100.0). */
    public double getPorcentajeCoincidencia() {
        if (totalRequisitos == 0) return 0.0;
        return (requisitosCumplidos * 100.0) / totalRequisitos;
    }

    public String getPorcentajeFormateado() {
        return String.format("%.2f%%", getPorcentajeCoincidencia());
    }

    public String getRequisitosFormateados() {
        return requisitosCumplidos + " / " + totalRequisitos;
    }
}
