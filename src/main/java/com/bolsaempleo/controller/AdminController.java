package com.bolsaempleo.controller;

import com.bolsaempleo.service.*;
import com.lowagie.text.DocumentException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.time.LocalDate;

/**
 * All administrator functionality:
 *  - Dashboard
 *  - Approve Empresas and Oferentes
 *  - CRUD of hierarchical Características
 *  - Monthly PDF report
 */
@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final EmpresaService        empresaService;
    private final OferenteService       oferenteService;
    private final CaracteristicaService caracteristicaService;
    private final ReportePdfService     reportePdfService;

    // ── Dashboard ─────────────────────────────────────────────────────────────

    @GetMapping("/dashboard")
    public String dashboard() {
        return "admin/dashboard";
    }

    // ── Empresas pendientes ───────────────────────────────────────────────────

    @GetMapping("/empresas/pendientes")
    public String empresasPendientes(Model model) {
        model.addAttribute("empresas", empresaService.obtenerPendientes());
        return "admin/empresas-pendientes";
    }

    @PostMapping("/empresas/{id}/aprobar")
    public String aprobarEmpresa(@PathVariable Long id, RedirectAttributes ra) {
        try {
            String clave = empresaService.aprobarYGenerarClave(id);
            ra.addFlashAttribute("success",
                "Empresa aprobada. Clave generada: " + clave);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/empresas/pendientes";
    }

    // ── Oferentes pendientes ──────────────────────────────────────────────────

    @GetMapping("/oferentes/pendientes")
    public String oferentesPendientes(Model model) {
        model.addAttribute("oferentes", oferenteService.obtenerPendientes());
        return "admin/oferentes-pendientes";
    }

    @PostMapping("/oferentes/{id}/aprobar")
    public String aprobarOferente(@PathVariable Long id, RedirectAttributes ra) {
        try {
            String clave = oferenteService.aprobarYGenerarClave(id);
            ra.addFlashAttribute("success",
                "Oferente aprobado. Clave generada: " + clave);
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/oferentes/pendientes";
    }

    // ── Características jerárquicas ───────────────────────────────────────────

    /**
     * GET /admin/caracteristicas?padreId={id}
     *
     * When padreId is null, shows root categories.
     * When padreId is provided, navigates into that category.
     */
    @GetMapping("/caracteristicas")
    public String caracteristicas(@RequestParam(value = "padreId", required = false) Long padreId,
                                   Model model) {
        if (padreId == null) {
            model.addAttribute("categorias", caracteristicaService.obtenerRaices());
            model.addAttribute("actual", null);
            model.addAttribute("padres", caracteristicaService.obtenerRaices());
        } else {
            var actual = caracteristicaService.findById(padreId).orElse(null);
            model.addAttribute("categorias", caracteristicaService.obtenerHijos(padreId));
            model.addAttribute("actual", actual);
            // Dropdown for the "add" form: show all as potential parents
            model.addAttribute("padres", caracteristicaService.findAll());
        }
        model.addAttribute("todas", caracteristicaService.findAll());
        return "admin/caracteristicas";
    }

    @PostMapping("/caracteristicas/crear")
    public String crearCaracteristica(@RequestParam("nombre") String nombre,
                                       @RequestParam(value = "padreId", required = false) Long padreId,
                                       RedirectAttributes ra) {
        try {
            caracteristicaService.crear(nombre, padreId);
            ra.addFlashAttribute("success", "Característica creada correctamente.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        String redirect = padreId != null
                ? "redirect:/admin/caracteristicas?padreId=" + padreId
                : "redirect:/admin/caracteristicas";
        return redirect;
    }

    @PostMapping("/caracteristicas/{id}/eliminar")
    public String eliminarCaracteristica(@PathVariable Long id,
                                          @RequestParam(value = "padreId", required = false) Long padreId,
                                          RedirectAttributes ra) {
        try {
            caracteristicaService.eliminar(id);
            ra.addFlashAttribute("success", "Característica eliminada.");
        } catch (Exception e) {
            ra.addFlashAttribute("error",
                "No se puede eliminar: posiblemente tiene subcategorías o está en uso.");
        }
        String redirect = padreId != null
                ? "redirect:/admin/caracteristicas?padreId=" + padreId
                : "redirect:/admin/caracteristicas";
        return redirect;
    }

    // ── Reporte PDF mensual ───────────────────────────────────────────────────

    @GetMapping("/reportes")
    public String formReporte(Model model) {
        model.addAttribute("anioActual", LocalDate.now().getYear());
        model.addAttribute("mesActual",  LocalDate.now().getMonthValue());
        return "admin/reportes";
    }

    /**
     * GET /admin/reportes/pdf?anio=2026&mes=3
     * Streams the PDF directly to the browser.
     */
    @GetMapping("/reportes/pdf")
    public void generarPdf(@RequestParam int anio,
                            @RequestParam int mes,
                            HttpServletResponse response) throws IOException, DocumentException {
        byte[] pdf = reportePdfService.generarReporteMensual(anio, mes);
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "inline; filename=\"reporte_puestos_" + anio + "_" + String.format("%02d", mes) + ".pdf\"");
        response.setContentLength(pdf.length);
        response.getOutputStream().write(pdf);
        response.getOutputStream().flush();
    }
}
