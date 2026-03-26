package com.bolsaempleo.controller;

import com.bolsaempleo.dto.form.EmpresaRegistroForm;
import com.bolsaempleo.dto.form.OferenteRegistroForm;
import com.bolsaempleo.model.Empresa;
import com.bolsaempleo.model.Oferente;
import com.bolsaempleo.service.EmpresaService;
import com.bolsaempleo.service.OferenteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Handles public registration for Empresa and Oferente.
 * Accounts are created in a pending state; an admin must approve them
 * before the user can log in.
 */
@Controller
@RequestMapping("/registro")
@RequiredArgsConstructor
public class RegistroController {

    private final EmpresaService empresaService;
    private final OferenteService oferenteService;

    // ── Empresa ───────────────────────────────────────────────────────────────

    @GetMapping("/empresa")
    public String formEmpresa(Model model) {
        model.addAttribute("form", new EmpresaRegistroForm());
        return "public/registro-empresa";
    }

    @PostMapping("/empresa")
    public String registrarEmpresa(@Valid @ModelAttribute("form") EmpresaRegistroForm form,
                                   BindingResult result,
                                   RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "public/registro-empresa";
        }
        try {
            Empresa empresa = new Empresa();
            empresa.setNombre(form.getNombre());
            empresa.setLocalizacion(form.getLocalizacion());
            empresa.setCorreoElectronico(form.getCorreoElectronico());
            empresa.setTelefono(form.getTelefono());
            empresa.setDescripcion(form.getDescripcion());
            empresaService.registrar(empresa);
            ra.addFlashAttribute("success",
                "Registro enviado correctamente. Un administrador revisará su solicitud.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/registro/empresa";
    }

    // ── Oferente ──────────────────────────────────────────────────────────────

    @GetMapping("/oferente")
    public String formOferente(Model model) {
        model.addAttribute("form", new OferenteRegistroForm());
        return "public/registro-oferente";
    }

    @PostMapping("/oferente")
    public String registrarOferente(@Valid @ModelAttribute("form") OferenteRegistroForm form,
                                    BindingResult result,
                                    RedirectAttributes ra) {
        if (result.hasErrors()) {
            return "public/registro-oferente";
        }
        try {
            Oferente oferente = new Oferente();
            oferente.setIdentificacion(form.getIdentificacion());
            oferente.setNombre(form.getNombre());
            oferente.setPrimerApellido(form.getPrimerApellido());
            oferente.setNacionalidad(form.getNacionalidad());
            oferente.setTelefono(form.getTelefono());
            oferente.setCorreoElectronico(form.getCorreoElectronico());
            oferente.setLugarResidencia(form.getLugarResidencia());
            oferenteService.registrar(oferente);
            ra.addFlashAttribute("success",
                "Registro enviado correctamente. Un administrador revisará su solicitud.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/registro/oferente";
    }
}
