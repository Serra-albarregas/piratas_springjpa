package com.serra.piratas.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.serra.piratas.entities.Tripulacion;
import com.serra.piratas.repositories.TripulacionRepository;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/tripulaciones")
@AllArgsConstructor
public class TripulacionController {

    private final TripulacionRepository repo;

    @GetMapping
    public String listar(Model model) {
        // findAllByOrderByNombreAsc() NO trae los piratas ni los territorios: la plantilla los pide
        // al recorrerlos (N+1 a propósito, mira la consola). Para evitarlo:
        // repo.listarConTerritorios() carga los territorios en la misma consulta.
        model.addAttribute("tripulaciones", repo.findAllByOrderByNombreAsc());
        return "tripulaciones/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Tripulacion tripulacion = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Tripulación no encontrada"));
        model.addAttribute("tripulacion", tripulacion);
        return "tripulaciones/detalle";
    }
}
