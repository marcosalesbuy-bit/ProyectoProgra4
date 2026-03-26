package com.bolsaempleo.service;

import com.bolsaempleo.model.HabilidadOferente;
import com.bolsaempleo.model.Oferente;
import com.bolsaempleo.repository.HabilidadOferenteRepository;
import com.bolsaempleo.repository.OferenteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OferenteService {

    private final OferenteRepository oferenteRepository;
    private final HabilidadOferenteRepository habilidadRepository;
    private final PasswordEncoder passwordEncoder;

    /** Register a new oferente (unapproved, no password yet). */
    @Transactional
    public Oferente registrar(Oferente oferente) {
        if (oferenteRepository.existsByCorreoElectronico(oferente.getCorreoElectronico())) {
            throw new IllegalArgumentException("Ya existe un oferente con ese correo electrónico.");
        }
        if (oferenteRepository.existsByIdentificacion(oferente.getIdentificacion())) {
            throw new IllegalArgumentException("Ya existe un oferente con esa identificación.");
        }
        oferente.setAprobado(false);
        oferente.setClave(null);
        return oferenteRepository.save(oferente);
    }

    /** Oferentes waiting for admin approval. */
    public List<Oferente> obtenerPendientes() {
        return oferenteRepository.findByAprobadoFalse();
    }

    /** All approved oferentes. */
    public List<Oferente> obtenerAprobados() {
        return oferenteRepository.findByAprobadoTrue();
    }

    /**
     * Admin approves an oferente: generates a random password,
     * hashes it, saves it, marks aprobado=true,
     * and returns the plain-text password.
     */
    @Transactional
    public String aprobarYGenerarClave(Long oferenteId) {
        Oferente oferente = oferenteRepository.findById(oferenteId)
                .orElseThrow(() -> new IllegalArgumentException("Oferente no encontrado: " + oferenteId));
        String plain = generarClaveAleatoria();
        oferente.setClave(passwordEncoder.encode(plain));
        oferente.setAprobado(true);
        oferenteRepository.save(oferente);
        return plain;
    }

    public Optional<Oferente> findByCorreo(String correo) {
        return oferenteRepository.findByCorreoElectronico(correo);
    }

    public Optional<Oferente> findById(Long id) {
        return oferenteRepository.findById(id);
    }

    // ── Habilidades ───────────────────────────────────────────────────────────

    /** Returns all skills for an oferente, ordered by characteristic name. */
    public List<HabilidadOferente> obtenerHabilidades(Long oferenteId) {
        return habilidadRepository.findByOferenteId(oferenteId);
    }

    /** Add or update a skill (upsert). */
    @Transactional
    public void guardarHabilidad(Long oferenteId, Long caracteristicaId, int nivel) {
        habilidadRepository.findByOferenteIdAndCaracteristicaId(oferenteId, caracteristicaId)
                .ifPresentOrElse(
                        h -> {
                            h.setNivel(nivel);
                            habilidadRepository.save(h);
                        },
                        () -> {
                            Oferente oferente = oferenteRepository.findById(oferenteId)
                                    .orElseThrow();
                            HabilidadOferente nueva = new HabilidadOferente();
                            nueva.setOferente(oferente);
                            com.bolsaempleo.model.Caracteristica c = new com.bolsaempleo.model.Caracteristica();
                            c.setId(caracteristicaId);
                            nueva.setCaracteristica(c);
                            nueva.setNivel(nivel);
                            habilidadRepository.save(nueva);
                        }
                );
    }

    /** Remove a skill from an oferente. */
    @Transactional
    public void eliminarHabilidad(Long oferenteId, Long caracteristicaId) {
        habilidadRepository.deleteByOferenteIdAndCaracteristicaId(oferenteId, caracteristicaId);
    }

    /** Update CV path after file upload. */
    @Transactional
    public void actualizarCv(Long oferenteId, String cvPath) {
        Oferente oferente = oferenteRepository.findById(oferenteId)
                .orElseThrow(() -> new IllegalArgumentException("Oferente no encontrado: " + oferenteId));
        oferente.setCvPath(cvPath);
        oferenteRepository.save(oferente);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private String generarClaveAleatoria() {
        byte[] bytes = new byte[9];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
