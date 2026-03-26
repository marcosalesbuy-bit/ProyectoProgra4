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
@Table(name = "empresa")
@Getter
@Setter
@NoArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(length = 250)
    private String localizacion;

    @NotBlank
    @Email
    @Column(name = "correo_electronico", nullable = false, unique = true, length = 150)
    private String correoElectronico;

    @Column(length = 30)
    private String telefono;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    /** BCrypt-hashed password. Null until the admin approves and generates it. */
    @Column(length = 255)
    private String clave;

    @Column(nullable = false)
    private boolean aprobada = false;

    @Column(name = "fecha_registro", nullable = false, updatable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Puesto> puestos = new ArrayList<>();
}
