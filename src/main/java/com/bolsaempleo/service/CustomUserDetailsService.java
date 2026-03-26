package com.bolsaempleo.service;

import com.bolsaempleo.model.Administrador;
import com.bolsaempleo.model.Empresa;
import com.bolsaempleo.model.Oferente;
import com.bolsaempleo.repository.AdministradorRepository;
import com.bolsaempleo.repository.EmpresaRepository;
import com.bolsaempleo.repository.OferenteRepository;
import com.bolsaempleo.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Resolves a login username to the correct user type.
 *
 * Lookup order:
 *  1. Administrador — matched by identificacion (allows numeric IDs as login)
 *  2. Empresa       — matched by correoElectronico
 *  3. Oferente      — matched by correoElectronico
 *
 * If the resolved user is an Empresa or Oferente that has not been approved yet
 * (clave == null), a UsernameNotFoundException is thrown so Spring Security
 * rejects the attempt with a "bad credentials" message.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AdministradorRepository adminRepo;
    private final EmpresaRepository       empresaRepo;
    private final OferenteRepository      oferenteRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 1. Admin by identificacion
        var adminOpt = adminRepo.findByIdentificacion(username);
        if (adminOpt.isPresent()) {
            Administrador a = adminOpt.get();
            return new CustomUserDetails(a.getId(), a.getIdentificacion(), a.getClave(), "ROLE_ADMIN");
        }

        // 2. Empresa by email
        var empresaOpt = empresaRepo.findByCorreoElectronico(username);
        if (empresaOpt.isPresent()) {
            Empresa e = empresaOpt.get();
            if (!e.isAprobada() || e.getClave() == null) {
                throw new UsernameNotFoundException("Cuenta pendiente de aprobación: " + username);
            }
            return new CustomUserDetails(e.getId(), e.getCorreoElectronico(), e.getClave(), "ROLE_EMPRESA");
        }

        // 3. Oferente by email
        var oferenteOpt = oferenteRepo.findByCorreoElectronico(username);
        if (oferenteOpt.isPresent()) {
            Oferente o = oferenteOpt.get();
            if (!o.isAprobado() || o.getClave() == null) {
                throw new UsernameNotFoundException("Cuenta pendiente de aprobación: " + username);
            }
            return new CustomUserDetails(o.getId(), o.getCorreoElectronico(), o.getClave(), "ROLE_OFERENTE");
        }

        throw new UsernameNotFoundException("Usuario no encontrado: " + username);
    }
}
