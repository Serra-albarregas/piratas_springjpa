package com.serra.piratas.controllers;

import com.serra.piratas.entities.Pirata;
import com.serra.piratas.repositories.PirataRepository;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@Controller
@RequestMapping("/piratas")
@AllArgsConstructor
public class PirataController {

    private final PirataRepository repo;

    private static final List<String> CAMPOS_ORDEN = List.of("recompensa", "nombre", "tripulacion");
    private static final List<Integer> TAMANOS = List.of(4, 8, 12);

    @GetMapping
    public String listar(@RequestParam(required = false) String nombre,
            @RequestParam(required = false) String tripulacion,
            @RequestParam(required = false) Long recompensaMin,
            @RequestParam(defaultValue = "recompensa") String orden,
            @RequestParam(defaultValue = "desc") String dir,
            @RequestParam(defaultValue = "4") int size,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // 1. Normalizar: nunca nos fiamos de lo que llega por la URL
        nombre = vacio(nombre) ? null : nombre.trim();
        tripulacion = vacio(tripulacion) ? null : tripulacion.trim();
        String campo = CAMPOS_ORDEN.contains(orden) ? orden : "recompensa";
        Sort.Direction direccion = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        size = TAMANOS.contains(size) ? size : TAMANOS.get(0);
        page = Math.max(page, 0);

        // 2. Orden estable: desempate por id para que no se repitan ni se salten filas entre páginas
        Sort sort = Sort.by(direccion, campo).and(Sort.by(Sort.Direction.ASC, "id"));
        Page<Pirata> pagina = repo.filtrar(nombre, tripulacion, recompensaMin, PageRequest.of(page, size, sort));

        // 3. Si piden una página que ya no existe (p. ej. al filtrar desde la página 3), vamos a la última
        if (pagina.getTotalPages() > 0 && page >= pagina.getTotalPages()) {
            page = pagina.getTotalPages() - 1;
            pagina = repo.filtrar(nombre, tripulacion, recompensaMin, PageRequest.of(page, size, sort));
        }

        // 4. URL base con todos los filtros (menos la página) para construir los enlaces de la paginación
        String dirTexto = direccion.name().toLowerCase();
        UriComponentsBuilder uri = UriComponentsBuilder.fromPath("/piratas");
        if (nombre != null)
            uri.queryParam("nombre", nombre);
        if (tripulacion != null)
            uri.queryParam("tripulacion", tripulacion);
        if (recompensaMin != null)
            uri.queryParam("recompensaMin", recompensaMin);
        uri.queryParam("orden", campo).queryParam("dir", dirTexto).queryParam("size", size);

        // 5. Añadir todos los datos al modelo, en este ejemplo mandamos la página y los piratas por separado, pero
        // podríamos simplemente añadir la página y usar getContent() en la plantilla para recorrer la colección
        model.addAttribute("pagina", pagina);
        model.addAttribute("piratas", pagina.getContent());
        model.addAttribute("tripulaciones", repo.listarTripulaciones());
        model.addAttribute("nombre", nombre);
        model.addAttribute("tripulacion", tripulacion);
        model.addAttribute("recompensaMin", recompensaMin);
        model.addAttribute("orden", campo);
        model.addAttribute("dir", dirTexto);
        model.addAttribute("size", size);
        model.addAttribute("tamanos", TAMANOS);
        model.addAttribute("urlBase", uri.build().encode().toUriString() + "&page=");
        model.addAttribute("inicioVentana", Math.max(0, page - 2));
        model.addAttribute("finVentana", Math.min(pagina.getTotalPages() - 1, page + 2));
        return "piratas/lista";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Long id, Model model) {
        Pirata pirata = repo.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pirata no encontrado"));
        model.addAttribute("pirata", pirata);
        return "piratas/detalle";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("pirata", new Pirata());
        return "piratas/formulario";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("pirata") Pirata pirata,
            BindingResult result,
            RedirectAttributes flash) {
        if (result.hasErrors()) {
            return "piratas/formulario";
        }
        repo.save(pirata);
        flash.addFlashAttribute("mensaje", "¡" + pirata.getNombre() + " se ha hecho a la mar!");
        return "redirect:/piratas";
    }

    private boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}