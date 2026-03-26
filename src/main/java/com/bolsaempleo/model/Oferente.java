package com.bolsaempleo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "oferente")
@Getter
@Setter
@NoArgsConstructor
public class Oferente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 30)
    private String identificacion;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank
    @Column(name = "primer_apellido", nullable = false, length = 100)
    private String primerApellido;

    @Column(length = 60)
    private String nacionalidad;

    @Column(length = 30)
    private String telefono;

    @NotBlank
    @Email
    @Column(name = "correo_electronico", nullable = false, unique = true, length = 150)
    private String correoElectronico;

    @Column(name = "lugar_residencia", length = 200)
    private String lugarResidencia;

    /** BCrypt-hashed password. Null until admin approves. */
    @Column(length = 255)
    private String clave;

    @Column(nullable = false)
    private boolean aprobado = false;

    /** Relative path to uploaded CV PDF. */
    @Column(name = "cv_path", length = 500)
    private String cvPath;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @OneToMany(mappedBy = "oferente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HabilidadOferente> habilidades = new ArrayList<>();

    public String getNombreCompleto() {
        return nombre + " " + primerApellido;
    }
}
