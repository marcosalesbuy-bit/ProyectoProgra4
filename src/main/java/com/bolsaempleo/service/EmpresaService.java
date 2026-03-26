package com.bolsaempleo.service;

import com.bolsaempleo.model.Empresa;
import com.bolsaempleo.repository.EmpresaRepository;
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
public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final PasswordEncoder passwordEncoder;

    /** Register a new empresa (unapproved, no password yet). */
    @Transactional
    public Empresa registrar(Empresa empresa) {
        if (empresaRepository.existsByCorreoElectronico(empresa.getCorreoElectronico())) {
            throw new IllegalArgumentException("Ya existe una empresa con ese correo electrónico.");
        }
        empresa.setAprobada(false);
        empresa.setClave(null);
        return empresaRepository.save(empresa);
    }

    /** Empresas that haven't been approved yet. */
    public List<Empresa> obtenerPendientes() {
        return empresaRepository.findByAprobadaFalse();
    }

    /**
     * Admin approves an empresa: generates a random plain-text password,
     * hashes it, saves it, marks aprobada=true, and returns the plain-text
     * password so the admin can communicate it to the empresa.
     */
    @Transactional
    public String aprobarYGenerarClave(Long empresaId) {
        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada: " + empresaId));
        String plain = generarClaveAleatoria();
        empresa.setClave(passwordEncoder.encode(plain));
        empresa.setAprobada(true);
        empresaRepository.save(empresa);
        return plain;
    }

    public Optional<Empresa> findByCorreo(String correo) {
        return empresaRepository.findByCorreoElectronico(correo);
    }

    public Optional<Empresa> findById(Long id) {
        return empresaRepository.findById(id);
    }

    public List<Empresa> findAll() {
        return empresaRepository.findAll();
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private String generarClaveAleatoria() {
        byte[] bytes = new byte[9]; // 12 base64 chars
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
