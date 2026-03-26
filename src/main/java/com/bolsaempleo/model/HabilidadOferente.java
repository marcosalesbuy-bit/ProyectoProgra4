package com.bolsaempleo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Represents one skill of a job seeker at a specific level.
 * e.g. Oferente "Juan" has Java at level 5.
 */
@Entity
@Table(name = "habilidad_oferente",
       uniqueConstraints = @UniqueConstraint(columnNames = {"oferente_id", "caracteristica_id"}))
@Getter
@Setter
@NoArgsConstructor
public class HabilidadOferente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "oferente_id", nullable = false)
    private Oferente oferente;

    @NotNull
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "caracteristica_id", nullable = false)
    private Caracteristica caracteristica;

    /** Self-reported proficiency level (1–5). */
    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int nivel;
}
