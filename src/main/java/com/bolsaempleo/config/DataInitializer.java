package com.bolsaempleo.config;

import com.bolsaempleo.model.Administrador;
import com.bolsaempleo.model.Caracteristica;
import com.bolsaempleo.repository.AdministradorRepository;
import com.bolsaempleo.repository.CaracteristicaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Seeds the database with a default admin account and initial characteristic
 * tree on first run (idempotent — skips if data already exists).
 */
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final AdministradorRepository adminRepo;
    private final CaracteristicaRepository caracteristicaRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedCaracteristicas();
    }

    // ── Admin ─────────────────────────────────────────────────────────────────

    private void seedAdmin() {
        if (adminRepo.findByIdentificacion("admin").isEmpty()) {
            Administrador a = new Administrador();
            a.setIdentificacion("admin");
            a.setNombre("Administrador del Sistema");
            a.setClave(passwordEncoder.encode("admin1234"));
            adminRepo.save(a);
            System.out.println("[DataInitializer] Admin creado — identificacion: admin | clave: admin1234");
        }
    }

    // ── Characteristics ───────────────────────────────────────────────────────

    private void seedCaracteristicas() {
        if (caracteristicaRepo.count() > 0) return;

        // Root nodes
        Caracteristica bd      = crearRaiz("Bases de Datos");
        Caracteristica ciber   = crearRaiz("Ciberseguridad");
        Caracteristica langs   = crearRaiz("Lenguajes de programación");
        Caracteristica web     = crearRaiz("Tecnologías Web");
        Caracteristica testing = crearRaiz("Testing");
        Caracteristica model   = crearRaiz("Modelado");

        // Bases de Datos → Motores
        Caracteristica motores = crearHijo("Motores", bd);
        crearHijo("MySQL",     motores);
        crearHijo("Oracle",    motores);
        crearHijo("PostgreSQL",motores);
        crearHijo("SQL Server",motores);

        // Lenguajes de programación
        crearHijo("C#",     langs);
        crearHijo("Java",   langs);
        crearHijo("Kotlin", langs);
        crearHijo("Python", langs);

        // Tecnologías Web
        crearHijo("HTML",        web);
        crearHijo("CSS",         web);
        crearHijo("JavaScript",  web);
        crearHijo("Spring Boot", web);
        crearHijo("Thymeleaf",   web);

        // Testing → JUnit
        Caracteristica junit = crearHijo("JUnit", testing);
        crearHijo("Assertions", junit);
        crearHijo("Test cases", junit);

        // Ciberseguridad
        crearHijo("OWASP",      ciber);
        crearHijo("Pentesting", ciber);

        // Modelado
        crearHijo("UML", model);

        System.out.println("[DataInitializer] Características sembradas correctamente.");
    }

    private Caracteristica crearRaiz(String nombre) {
        Caracteristica c = new Caracteristica();
        c.setNombre(nombre);
        return caracteristicaRepo.save(c);
    }

    private Caracteristica crearHijo(String nombre, Caracteristica padre) {
        Caracteristica c = new Caracteristica();
        c.setNombre(nombre);
        c.setPadre(padre);
        return caracteristicaRepo.save(c);
    }
}
