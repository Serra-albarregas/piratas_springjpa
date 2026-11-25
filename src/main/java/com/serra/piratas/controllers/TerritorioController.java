package com.serra.piratas.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.serra.piratas.services.TerritorioService;

import lombok.AllArgsConstructor;

@Controller
@RequestMapping("/territorios")
@AllArgsConstructor
public class TerritorioController {

    private final TerritorioService service;

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("territorios", service.listarConTripulaciones());
        return "territorios/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        model.addAttribute("territorio", service.obtener(id));
        return "territorios/detalle";
    }
}
