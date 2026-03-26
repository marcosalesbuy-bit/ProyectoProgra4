package com.bolsaempleo.controller;

import com.bolsaempleo.service.PuestoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Public home page — shows the 5 most recently published public puestos.
 */
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final PuestoService puestoService;

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("puestos", puestoService.obtenerUltimosPublicos());
        return "public/home";
    }
}
