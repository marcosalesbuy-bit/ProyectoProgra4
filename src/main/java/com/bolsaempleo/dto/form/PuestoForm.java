package com.bolsaempleo.dto.form;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Form backing object for creating / editing a Puesto.
 *
 * The `niveles` map associates each Caracteristica ID with the required
 * proficiency level chosen by the empresa.
 *   - A value of 0 means the characteristic is NOT required (excluded).
 *   - A value of 1–5 means the characteristic IS required at that level.
 *
 * Thymeleaf binds each row as:
 *   th:field="*{niveles[__${c.id}__]}"
 */
@Getter
@Setter
public class PuestoForm {

    @NotBlank(message = "La descripción del puesto es obligatoria")
    private String descripcion;

    private BigDecimal salarioOfrecido;

    private boolean esPublico = true;

    /**
     * Key   = Caracteristica.id
     * Value = nivel requerido (0 = no requerido, 1–5 = nivel mínimo)
     */
    private Map<Long, Integer> niveles = new HashMap<>();
}
