package com.bolsaempleo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Hierarchical skill category managed by the administrator.
 * A null padre means it is a root category.
 * Example tree:
 *   Lenguajes de programación (padre=null)
 *     ├── Java               (padre=Lenguajes de programación)
 *     └── C#                 (padre=Lenguajes de programación)
 *   Tecnologías Web          (padre=null)
 *     ├── HTML
 *     ├── CSS
 *     └── JavaScript
 */
@Entity
@Table(name = "caracteristica")
@Getter
@Setter
@NoArgsConstructor
public class Caracteristica {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 120)
    private String nombre;

    /** Null for root categories. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "padre_id")
    private Caracteristica padre;

    @OneToMany(mappedBy = "padre", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Caracteristica> hijos = new ArrayList<>();

    /** Convenience: true when this node has no children. */
    @Transient
    public boolean isHoja() {
        return hijos == null || hijos.isEmpty();
    }

    /**
     * Builds a breadcrumb path string like "Testing / JUnit / Assertions".
     */
    public String getRuta() {
        if (padre == null) {
            return nombre;
        }
        return padre.getRuta() + " / " + nombre;
    }
}
