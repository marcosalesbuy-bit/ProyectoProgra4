package com.bolsaempleo.controller;

import com.bolsaempleo.model.Oferente;
import com.bolsaempleo.security.CustomUserDetails;
import com.bolsaempleo.service.CaracteristicaService;
import com.bolsaempleo.service.CvStorageService;
import com.bolsaempleo.service.OferenteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * All functionality for authenticated Oferentes:
 *  - Dashboard
 *  - CRUD de habilidades
 *  - Subir CV (PDF)
 */
@Controller
@RequestMapping("/oferente")
@RequiredArgsConstructor
public class OferenteController {

    private final OferenteService       oferenteService;
    private final CaracteristicaService caracteristicaService;
    private final CvStorageService      cvStorageService;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        Oferente oferente = getOferente(user);
        model.addAttribute("oferente", oferente);
        return "oferente/dashboard";
    }

    // ── Mis Habilidades ───────────────────────────────────────────────────────

    /**
     * GET /oferente/habilidades?actualid={id}
     *
     * Shows the habilidades list and the "Agregar habilidad" form.
     * If `actualid` is provided, the characteristic tree is navigated
     * to that node so the user can select a sub-characteristic to add.
     *
     * @param actualid  ID of the current node being browsed (null = roots)
     */
    @GetMapping("/habilidades")
    public String misHabilidades(@AuthenticationPrincipal CustomUserDetails user,
                                  @RequestParam(value = "actualid", required = false) Long actualid,
                                  Model model) {
        Long oferenteId = user.getId();
        model.addAttribute("oferente", getOferente(user));
        model.addAttribute("habilidades", oferenteService.obtenerHabilidades(oferenteId));

        // Navigate the characteristic tree for the add-skill form
        if (actualid == null) {
            model.addAttribute("hijos", caracteristicaService.obtenerRaices());
            model.addAttribute("actual", null);
        } else {
            var actual = caracteristicaService.findById(actualid).orElse(null);
            model.addAttribute("actual", actual);
            model.addAttribute("hijos", caracteristicaService.obtenerHijos(actualid));
        }
        return "oferente/habilidades";
    }

    /**
     * POST /oferente/habilidades/agregar
     * Adds or updates a skill for the authenticated oferente.
     */
    @PostMapping("/habilidades/agregar")
    public String agregarHabilidad(@AuthenticationPrincipal CustomUserDetails user,
                                    @RequestParam("caracteristicaId") Long caracteristicaId,
                                    @RequestParam("nivel") int nivel,
                                    RedirectAttributes ra) {
        if (nivel < 1 || nivel > 5) {
            ra.addFlashAttribute("error", "El nivel debe estar entre 1 y 5.");
            return "redirect:/oferente/habilidades";
        }
        try {
            oferenteService.guardarHabilidad(user.getId(), caracteristicaId, nivel);
            ra.addFlashAttribute("success", "Habilidad guardada correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/oferente/habilidades";
    }

    /**
     * POST /oferente/habilidades/{habilidadId}/eliminar
     * Removes a skill from the authenticated oferente.
     */
    @PostMapping("/habilidades/{caracteristicaId}/eliminar")
    public String eliminarHabilidad(@AuthenticationPrincipal CustomUserDetails user,
                                     @PathVariable Long caracteristicaId,
                                     RedirectAttributes ra) {
        try {
            oferenteService.eliminarHabilidad(user.getId(), caracteristicaId);
            ra.addFlashAttribute("success", "Habilidad eliminada.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/oferente/habilidades";
    }

    // ── Mi CV ─────────────────────────────────────────────────────────────────

    @GetMapping("/cv")
    public String verCv(@AuthenticationPrincipal CustomUserDetails user, Model model) {
        model.addAttribute("oferente", getOferente(user));
        return "oferente/cv";
    }

    /**
     * POST /oferente/cv/subir
     * Accepts a PDF upload, stores it, and updates the oferente's cvPath.
     */
    @PostMapping("/cv/subir")
    public String subirCv(@AuthenticationPrincipal CustomUserDetails user,
                           @RequestParam("cvFile") MultipartFile file,
                           RedirectAttributes ra) {
        try {
            String path = cvStorageService.store(file, user.getId());
            oferenteService.actualizarCv(user.getId(), path);
            ra.addFlashAttribute("success", "CV subido correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error al subir el CV: " + e.getMessage());
        }
        return "redirect:/oferente/cv";
    }

    // ── private helpers ───────────────────────────────────────────────────────

    private Oferente getOferente(CustomUserDetails user) {
        return oferenteService.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("Oferente no encontrado"));
    }
}
