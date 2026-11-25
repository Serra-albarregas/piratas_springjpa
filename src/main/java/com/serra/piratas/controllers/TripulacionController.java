package com.serra.piratas.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.serra.piratas.services.TripulacionService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/tripulaciones")
@AllArgsConstructor
public class TripulacionController {

    private final TripulacionService service;

    @GetMapping
    public String listar(Model model) {
        // DTO de salida: una consulta, con los recuentos ya calculados
        model.addAttribute("tripulaciones", service.listarResumen());
        return "tripulaciones/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("tripulacion", service.obtenerConDetalle(id));
        model.addAttribute("otras", service.listarOtras(id));
        return "tripulaciones/detalle";
    }

    // UPDATE masivo (@Modifying) dentro de una transacción abierta en el servicio
    @PostMapping("/{id}/recompensa")
    public String subirRecompensa(@PathVariable Long id,
            @RequestParam Long incremento,
            RedirectAttributes flash) {
        if (incremento == null || incremento <= 0) {
            flash.addFlashAttribute("error", "El incremento debe ser mayor que cero");
        } else {
            int filas = service.subirRecompensa(id, incremento);
            flash.addFlashAttribute("mensaje", "Recompensa subida a " + filas + " pirata(s)");
        }
        return "redirect:/tripulaciones/" + id;
    }

    // Operación de varios pasos: una sola transacción
    @PostMapping("/{id}/fusionar")
    public String fusionar(@PathVariable Long id,
            @RequestParam Long destinoId,
            @RequestParam(defaultValue = "false") boolean simularFallo,
            RedirectAttributes flash) {
        try {
            int movidos = service.fusionar(id, destinoId, simularFallo);
            flash.addFlashAttribute("mensaje", "Fusión completada: " + movidos + " pirata(s) pasan a esta tripulación");
            return "redirect:/tripulaciones/" + destinoId;
        } catch (IllegalStateException e) {
            // Se captura FUERA del servicio: la transacción ya se ha deshecho (rollback)
            // y la base de datos está exactamente como antes de empezar.
            flash.addFlashAttribute("error", e.getMessage() + ". Se ha hecho rollback: no ha cambiado nada.");
            return "redirect:/tripulaciones/" + id;
        }
    }
}
