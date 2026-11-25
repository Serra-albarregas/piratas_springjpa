package com.serra.piratas.services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serra.piratas.dto.PirataForm;
import com.serra.piratas.entities.FrutaDelDiablo;
import com.serra.piratas.entities.Pirata;
import com.serra.piratas.entities.Tripulacion;
import com.serra.piratas.exceptions.RecursoNoEncontradoException;
import com.serra.piratas.repositories.FrutaDelDiabloRepository;
import com.serra.piratas.repositories.PirataRepository;
import com.serra.piratas.repositories.TripulacionRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
// Por defecto, todos los métodos son de SOLO LECTURA. Los que escriben llevan su propio @Transactional,
// que tiene prioridad sobre el de la clase.
@Transactional(readOnly = true)
public class PirataService {

    private final PirataRepository repo;
    private final TripulacionRepository tripulacionRepo;
    private final FrutaDelDiabloRepository frutaRepo;

    // ═══════════════ LECTURA ═══════════════

    public Page<Pirata> filtrar(String nombre, Long tripulacionId, Long recompensaMin, Pageable pageable) {
        Page<Pirata> pagina = repo.filtrar(nombre, tripulacionId, recompensaMin, pageable);

        // Si piden una página que ya no existe (p. ej. al filtrar desde la página 3), devolvemos la última
        if (pagina.getTotalPages() > 0 && pageable.getPageNumber() >= pagina.getTotalPages()) {
            pagina = repo.filtrar(nombre, tripulacionId, recompensaMin,
                    pageable.withPage(pagina.getTotalPages() - 1));
        }
        return pagina;
    }

    // Ficha del pirata con todo lo que la vista necesita ya cargado (join fetch en el repositorio).
    // Si aquí se usara repo.findById(id), con open-in-view=false la plantilla fallaría al leer
    // pirata.tripulacion.territorios (LazyInitializationException).
    public Pirata obtener(Long id) {
        return repo.buscarConDetalle(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el pirata " + id));
    }

    // Reglas que Bean Validation no puede comprobar porque necesitan consultar la base de datos
    public boolean existeNombre(String nombre) {
        return repo.findByNombreIgnoreCase(nombre).isPresent();
    }

    // La columna fruta_id es única (uno a uno): una fruta solo puede tener un dueño
    public boolean frutaOcupada(Long frutaId) {
        return repo.existsByFrutaId(frutaId);
    }

    // ═══════════════ ESCRITURA ═══════════════

    // Convierte el formulario (con identificadores) en una entidad (con objetos)
    @Transactional
    public Pirata crear(PirataForm form) {
        Tripulacion tripulacion = tripulacionRepo.findById(form.getTripulacionId())
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe la tripulación " + form.getTripulacionId()));

        FrutaDelDiablo fruta = null;
        if (form.getFrutaId() != null) {
            fruta = frutaRepo.findById(form.getFrutaId())
                    .orElseThrow(() -> new RecursoNoEncontradoException(
                            "No existe la fruta " + form.getFrutaId()));
        }

        Pirata pirata = new Pirata();
        pirata.setNombre(form.getNombre().trim());
        pirata.setTripulacion(tripulacion);
        pirata.setFruta(fruta);
        pirata.setRecompensa(form.getRecompensa());
        pirata.setDescripcion(form.getDescripcion());
        return repo.save(pirata);
    }
}
