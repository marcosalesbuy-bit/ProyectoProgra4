package com.bolsaempleo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "puesto")
@Getter
@Setter
@NoArgsConstructor
public class Puesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empresa_id", nullable = false)
    private Empresa empresa;

    @NotBlank
    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @PositiveOrZero
    @Column(name = "salario_ofrecido", precision = 10, scale = 2)
    private BigDecimal salarioOfrecido;

    /** true = visible to anyone; false = visible only to approved oferentes. */
    @Column(name = "es_publico", nullable = false)
    private boolean esPublico = true;

    /** false when the company deactivates the position. */
    @Column(nullable = false)
    private boolean activo = true;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @OneToMany(mappedBy = "puesto", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RequisitoPuesto> requisitos = new ArrayList<>();
}
