package com.bolsaempleo.controller;

import com.bolsaempleo.dto.MatchingResultDTO;
import com.bolsaempleo.dto.form.PuestoForm;
import com.bolsaempleo.model.Empresa;
import com.bolsaempleo.model.Oferente;
import com.bolsaempleo.model.Puesto;
import com.bolsaempleo.security.CustomUserDetails;
import com.bolsaempleo.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * All functionality for authenticated Empresas:
 *  - Dashboard
 *  - CRUD de puestos
 *  - Buscar candidatos (matching)
 *  - Ver detalle de candidato + descargar CV
 */
@Controller
@RequestMapping("/empresa")
@RequiredArgsConstructor
public class EmpresaController {

    private final EmpresaService     empresaService;
    private final PuestoService      puestoService;
    private final CaracteristicaService caracteristicaService;
    private final MatchingService    matchingService;
    private final OferenteService    oferenteService;
    private final CvStorageService   cvStorageService;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        Empresa empresa = getEmpresa(user);
        model.addAttribute("empresa", empresa);
        return "empresa/dashboard";
    }

    // ── Mis Puestos ───────────────────────────────────────────────────────────

    @GetMapping("/puestos")
    public String listarPuestos(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("puestos", puestoService.obtenerPorEmpresa(user.getId()));
        return "empresa/mis-puestos";
    }

    // ── Publicar nuevo puesto ─────────────────────────────────────────────────

    @GetMapping("/puestos/nuevo")
    public String formNuevoPuesto(Model model) {
        model.addAttribute("form", new PuestoForm());
        model.addAttribute("caracteristicas", caracteristicaService.findAll());
        return "empresa/form-puesto";
    }

    @PostMapping("/puestos/nuevo")
    public String publicarPuesto(@AuthenticationPrincipal CustomUserDetails user,
                                 @Valid @ModelAttribute("form") PuestoForm form,
                                 BindingResult result,
                                 RedirectAttributes ra,
                                 Model model) {
        if (result.hasErrors()) {
            model.addAttribute("caracteristicas", caracteristicaService.findAll());
            return "empresa/form-puesto";
        }
        Empresa empresa = getEmpresa(user);
        try {
            List<Long>    ids    = new ArrayList<>();
            List<Integer> niveles = new ArrayList<>();
            extractNiveles(form.getNiveles(), ids, niveles);

            puestoService.publicar(empresa, form.getDescripcion(),
                    form.getSalarioOfrecido(), form.isEsPublico(), ids, niveles);
            ra.addFlashAttribute("success", "Puesto publicado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empresa/puestos";
    }

    // ── Editar puesto ─────────────────────────────────────────────────────────

    @GetMapping("/puestos/{id}/editar")
    public String formEditarPuesto(@PathVariable Long id,
                                   @AuthenticationPrincipal CustomUserDetails user,
                                   Model model) {
        Puesto puesto = getPuestoDeEmpresa(id, user.getId());
        PuestoForm form = new PuestoForm();
        form.setDescripcion(puesto.getDescripcion());
        form.setSalarioOfrecido(puesto.getSalarioOfrecido());
        form.setEsPublico(puesto.isEsPublico());
        puesto.getRequisitos().forEach(r ->
                form.getNiveles().put(r.getCaracteristica().getId(), r.getNivelRequerido()));

        model.addAttribute("form", form);
        model.addAttribute("puestoId", id);
        model.addAttribute("caracteristicas", caracteristicaService.findAll());
        return "empresa/form-puesto";
    }

    @PostMapping("/puestos/{id}/editar")
    public String actualizarPuesto(@PathVariable Long id,
                                   @AuthenticationPrincipal CustomUserDetails user,
                                   @Valid @ModelAttribute("form") PuestoForm form,
                                   BindingResult result,
                                   RedirectAttributes ra,
                                   Model model) {
        if (result.hasErrors()) {
            model.addAttribute("puestoId", id);
            model.addAttribute("caracteristicas", caracteristicaService.findAll());
            return "empresa/form-puesto";
        }
        getPuestoDeEmpresa(id, user.getId()); // ownership check
        try {
            List<Long>    ids    = new ArrayList<>();
            List<Integer> niveles = new ArrayList<>();
            extractNiveles(form.getNiveles(), ids, niveles);

            puestoService.actualizar(id, form.getDescripcion(),
                    form.getSalarioOfrecido(), form.isEsPublico(), ids, niveles);
            ra.addFlashAttribute("success", "Puesto actualizado correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/empresa/puestos";
    }

    // ── Desactivar puesto ─────────────────────────────────────────────────────

    @PostMapping("/puestos/{id}/desactivar")
    public String desactivarPuesto(@PathVariable Long id,
                                   @AuthenticationPrincipal CustomUserDetails user,
                                   RedirectAttributes ra) {
        getPuestoDeEmpresa(id, user.getId()); // ownership check
        puestoService.desactivar(id);
        ra.addFlashAttribute("success", "Puesto desactivado.");
        return "redirect:/empresa/puestos";
    }

    // ── Buscar candidatos (matching) ──────────────────────────────────────────

    @GetMapping("/candidatos/buscar")
    public String buscarCandidatos(@RequestParam("puestoId") Long puestoId,
                                   @AuthenticationPrincipal CustomUserDetails user,
                                   Model model) {
        Puesto puesto = getPuestoDeEmpresa(puestoId, user.getId());
        List<MatchingResultDTO> resultados = matchingService.buscarCandidatos(puesto);
        model.addAttribute("puesto", puesto);
        model.addAttribute("resultados", resultados);
        return "empresa/candidatos";
    }

    // ── Ver detalle de candidato ──────────────────────────────────────────────

    @GetMapping("/candidatos/{oferenteId}")
    public String verCandidato(@PathVariable Long oferenteId, Model model) {
        Oferente oferente = oferenteService.findById(oferenteId)
                .orElseThrow(() -> new IllegalArgumentException("Oferente no encontrado"));
        List<?> habilidades = oferenteService.obtenerHabilidades(oferenteId);
        model.addAttribute("oferente", oferente);
        model.addAttribute("habilidades", habilidades);
        return "empresa/detalle-candidato";
    }

    // ── Descargar CV del candidato ────────────────────────────────────────────

    @GetMapping("/candidatos/{oferenteId}/cv")
    public ResponseEntity<Resource> descargarCv(@PathVariable Long oferenteId) throws MalformedURLException {
        Oferente oferente = oferenteService.findById(oferenteId)
                .orElseThrow(() -> new IllegalArgumentException("Oferente no encontrado"));
        if (oferente.getCvPath() == null) {
            return ResponseEntity.notFound().build();
        }
        Path file = cvStorageService.load(oferente.getCvPath());
        Resource resource = new UrlResource(file.toUri());
        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"cv_" + oferenteId + ".pdf\"")
                .body(resource);
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private Empresa getEmpresa(CustomUserDetails user) {
        return empresaService.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("Empresa no encontrada"));
    }

    private Puesto getPuestoDeEmpresa(Long puestoId, Long empresaId) {
        Puesto puesto = puestoService.findById(puestoId)
                .orElseThrow(() -> new IllegalArgumentException("Puesto no encontrado"));
        if (!puesto.getEmpresa().getId().equals(empresaId)) {
            throw new SecurityException("Acceso denegado al puesto " + puestoId);
        }
        return puesto;
    }

    /**
     * Extracts non-zero entries from the niveles map into parallel lists
     * suitable for PuestoService.publicar / actualizar.
     */
    private void extractNiveles(Map<Long, Integer> nivelesMap,
                                 List<Long> ids,
                                 List<Integer> niveles) {
        if (nivelesMap == null) return;
        nivelesMap.forEach((cid, nivel) -> {
            if (nivel != null && nivel > 0) {
                ids.add(cid);
                niveles.add(nivel);
            }
        });
    }
}
