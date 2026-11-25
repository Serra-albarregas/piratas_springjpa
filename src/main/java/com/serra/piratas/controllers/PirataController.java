package com.serra.piratas.controllers;

import com.serra.piratas.dto.PirataForm;
import com.serra.piratas.entities.Pirata;
import com.serra.piratas.services.FrutaDelDiabloService;
import com.serra.piratas.services.PirataService;
import com.serra.piratas.services.TripulacionService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

// El controlador recibe SERVICIOS, no repositorios: solo lee parámetros, valida el formulario
// y elige la vista. La lógica de negocio y las transacciones viven en la capa de servicio.
@Controller
@RequestMapping("/piratas")
@AllArgsConstructor
public class PirataController {

    private final PirataService pirataService;
    private final TripulacionService tripulacionService;
    private final FrutaDelDiabloService frutaService;

    // Nombre que llega por la URL -> propiedad por la que se ordena.
    // La tripulación ya no es un texto sino una entidad: se ordena por su nombre.
    private static final Map<String, String> CAMPOS_ORDEN = Map.of(
            "recompensa", "recompensa",
            "nombre", "nombre",
            "tripulacion", "tripulacion.nombre");
    private static final List<Integer> TAMANOS = List.of(4, 8, 12);

    @GetMapping
    public String listar(@RequestParam(required = false) String nombre,
            @RequestParam(required = false) Long tripulacionId,
            @RequestParam(required = false) Long recompensaMin,
            @RequestParam(defaultValue = "recompensa") String orden,
            @RequestParam(defaultValue = "desc") String dir,
            @RequestParam(defaultValue = "4") int size,
            @RequestParam(defaultValue = "0") int page,
            Model model) {

        // 1. Normalizar: nunca nos fiamos de lo que llega por la URL
        nombre = vacio(nombre) ? null : nombre.trim();
        String campo = CAMPOS_ORDEN.containsKey(orden) ? orden : "recompensa";
        Sort.Direction direccion = "asc".equalsIgnoreCase(dir) ? Sort.Direction.ASC : Sort.Direction.DESC;
        size = TAMANOS.contains(size) ? size : TAMANOS.get(0);
        page = Math.max(page, 0);

        // 2. Orden estable: desempate por id para que no se repitan ni se salten filas entre páginas
        Sort sort = Sort.by(direccion, CAMPOS_ORDEN.get(campo)).and(Sort.by(Sort.Direction.ASC, "id"));
        Page<Pirata> pagina = pirataService.filtrar(nombre, tripulacionId, recompensaMin,
                PageRequest.of(page, size, sort));

        // 3. El servicio ya ha corregido la página si no existía: usamos la que realmente devuelve
        page = pagina.getNumber();

        // 4. URL base con todos los filtros (menos la página) para construir los enlaces de la paginación
        String dirTexto = direccion.name().toLowerCase();
        UriComponentsBuilder uri = UriComponentsBuilder.fromPath("/piratas");
        if (nombre != null)
            uri.queryParam("nombre", nombre);
        if (tripulacionId != null)
            uri.queryParam("tripulacionId", tripulacionId);
        if (recompensaMin != null)
            uri.queryParam("recompensaMin", recompensaMin);
        uri.queryParam("orden", campo).queryParam("dir", dirTexto).queryParam("size", size);

        // 5. Añadir todos los datos al modelo
        model.addAttribute("pagina", pagina);
        model.addAttribute("piratas", pagina.getContent());
        model.addAttribute("tripulaciones", tripulacionService.listarParaSelect());
        model.addAttribute("nombre", nombre);
        model.addAttribute("tripulacionId", tripulacionId);
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
        // Si no existe, el servicio lanza RecursoNoEncontradoException y Spring responde con un 404
        model.addAttribute("pirata", pirataService.obtener(id));
        return "piratas/detalle";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("pirata", new PirataForm());
        cargarOpciones(model);
        return "piratas/formulario";
    }

    // Recibe un DTO de formulario (PirataForm), no la entidad
    @PostMapping
    public String guardar(@Valid @ModelAttribute("pirata") PirataForm form,
            BindingResult result,
            Model model,
            RedirectAttributes flash) {

        // Reglas que Bean Validation no puede comprobar porque necesitan consultar la base de datos.
        // Se informan como errores de campo, igual que los de las anotaciones.
        if (!result.hasFieldErrors("nombre") && pirataService.existeNombre(form.getNombre().trim())) {
            result.rejectValue("nombre", "duplicado", "Ya existe un pirata con ese nombre");
        }
        if (form.getFrutaId() != null && pirataService.frutaOcupada(form.getFrutaId())) {
            result.rejectValue("frutaId", "ocupada", "Esa Fruta del Diablo ya tiene dueño");
        }

        if (result.hasErrors()) {
            cargarOpciones(model);
            return "piratas/formulario";
        }
        Pirata creado = pirataService.crear(form);
        flash.addFlashAttribute("mensaje", "¡" + creado.getNombre() + " se ha hecho a la mar!");
        return "redirect:/piratas";
    }

    // Datos para los <select> del formulario
    private void cargarOpciones(Model model) {
        model.addAttribute("tripulaciones", tripulacionService.listarParaSelect());
        model.addAttribute("frutas", frutaService.listarLibres());
    }

    private boolean vacio(String s) {
        return s == null || s.isBlank();
    }
}
