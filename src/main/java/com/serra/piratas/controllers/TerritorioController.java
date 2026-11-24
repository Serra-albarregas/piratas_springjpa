package com.serra.piratas.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.serra.piratas.entities.Territorio;
import com.serra.piratas.repositories.TerritorioRepository;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/territorios")
@AllArgsConstructor
public class TerritorioController {

    private final TerritorioRepository repo;

    @GetMapping
    public String listar(Model model) {
        // Lado INVERSO del muchos a muchos: de cada territorio se llega a sus tripulaciones.
        // Con findAllByOrderByNombreAsc() habría una consulta extra por territorio (N+1);
        // listarConTripulaciones() las carga todas con un join fetch.
        model.addAttribute("territorios", repo.listarConTripulaciones());
        return "territorios/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Territorio territorio = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Territorio no encontrado"));
        model.addAttribute("territorio", territorio);
        return "territorios/detalle";
    }
}
