package com.bolsaempleo.controller;

import com.bolsaempleo.model.Caracteristica;
import com.bolsaempleo.model.Puesto;
import com.bolsaempleo.service.CaracteristicaService;
import com.bolsaempleo.service.PuestoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Public job-search page.
 * Allows anyone (including unauthenticated visitors) to search for public puestos
 * by selecting one or more characteristics from the hierarchical tree.
 */
@Controller
@RequestMapping("/puestos")
@RequiredArgsConstructor
public class PuestoPublicoController {

    private final PuestoService puestoService;
    private final CaracteristicaService caracteristicaService;

    /**
     * GET /puestos/buscar — display search form with the full characteristic tree.
     */
    @GetMapping("/buscar")
    public String formBuscar(Model model) {
        model.addAttribute("raices", caracteristicaService.obtenerRaices());
        return "public/buscar-puestos";
    }

    /**
     * POST /puestos/buscar — execute the search and return results on the same page.
     *
     * @param caracteristicaIds IDs of the selected characteristics (checkboxes)
     */
    @PostMapping("/buscar")
    public String buscar(@RequestParam(value = "caracteristicaIds", required = false)
                         List<Long> caracteristicaIds,
                         Model model) {
        List<Puesto> resultados = puestoService.buscarPublicos(caracteristicaIds);
        model.addAttribute("raices", caracteristicaService.obtenerRaices());
        model.addAttribute("resultados", resultados);
        model.addAttribute("seleccionados", caracteristicaIds);
        return "public/buscar-puestos";
    }
}
