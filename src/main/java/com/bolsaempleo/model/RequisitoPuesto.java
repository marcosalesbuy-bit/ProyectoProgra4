package com.bolsaempleo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents one skill requirement for a job position.
 * e.g. Puesto "Full Stack Developer" requires Java at level >= 4.
 */
@Entity
@Table(name = "requisito_puesto",
       uniqueConstraints = @UniqueConstraint(columnNames = {"puesto_id", "caracteristica_id"}))
@Getter
@Setter
@NoArgsConstructor
public class RequisitoPuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "puesto_id", nullable = false)
    private Puesto puesto;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "caracteristica_id", nullable = false)
    private Caracteristica caracteristica;

    /** Minimum level required (1–5). */
    @Min(1)
    @Max(5)
    @Column(name = "nivel_requerido", nullable = false)
    private int nivelRequerido;
}
