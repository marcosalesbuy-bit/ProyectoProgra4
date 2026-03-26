package com.bolsaempleo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "administrador")
@Getter
@Setter
@NoArgsConstructor
public class Administrador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, unique = true, length = 30)
    private String identificacion;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String nombre;

    /** BCrypt-hashed password. */
    @NotBlank
    @Column(nullable = false, length = 255)
    private String clave;
}
