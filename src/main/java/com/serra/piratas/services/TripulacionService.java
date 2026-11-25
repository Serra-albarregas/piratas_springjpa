package com.serra.piratas.services;

import java.util.List;

import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serra.piratas.dto.TripulacionResumen;
import com.serra.piratas.entities.Pirata;
import com.serra.piratas.entities.Territorio;
import com.serra.piratas.entities.Tripulacion;
import com.serra.piratas.exceptions.RecursoNoEncontradoException;
import com.serra.piratas.repositories.PirataRepository;
import com.serra.piratas.repositories.TripulacionRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class TripulacionService {

    private final TripulacionRepository repo;
    private final PirataRepository pirataRepo;

    // ═══════════════ LECTURA ═══════════════

    // Solución 3 de la página 10: devolver datos ya preparados (DTO), sin relaciones perezosas
    public List<TripulacionResumen> listarResumen() {
        return repo.listarResumen();
    }

    // Para los <select> de los formularios y filtros (solo se usan id y nombre)
    public List<Tripulacion> listarParaSelect() {
        return repo.findAllByOrderByNombreAsc();
    }

    public List<Tripulacion> listarOtras(Long id) {
        return repo.findAllByOrderByNombreAsc().stream()
                .filter(t -> !t.getId().equals(id))
                .toList();
    }

    // Solución 1 de la página 10: trabajar DENTRO de la transacción.
    // Las colecciones LAZY se inicializan aquí, mientras la sesión sigue abierta; después la
    // entidad queda separada, pero lo ya cargado se puede seguir leyendo desde la plantilla.
    public Tripulacion obtenerConDetalle(Long id) {
        Tripulacion tripulacion = buscar(id);
        Hibernate.initialize(tripulacion.getPiratas());
        Hibernate.initialize(tripulacion.getTerritorios());
        return tripulacion;
    }

    // ═══════════════ ESCRITURA ═══════════════

    /**
     * Fusiona dos tripulaciones: todos los piratas y territorios de la de origen pasan a la de destino
     * y la de origen se borra. Son varios pasos que forman UNA transacción:
     * si algo falla, se deshace todo y la base de datos queda como estaba.
     *
     * @param simularFallo SOLO PARA LA DEMOSTRACIÓN: lanza una excepción a mitad de la operación
     *                     para comprobar el rollback.
     * @return número de piratas movidos
     */
    @Transactional
    public int fusionar(Long origenId, Long destinoId, boolean simularFallo) {
        if (origenId.equals(destinoId)) {
            throw new IllegalArgumentException("No se puede fusionar una tripulación consigo misma");
        }
        Tripulacion origen = buscar(origenId);
        Tripulacion destino = buscar(destinoId);

        // Paso 1: cambiar la tripulación de cada pirata.
        // No hay ningún save: las entidades cargadas dentro de la transacción están gestionadas
        // por Hibernate, que al hacer commit detecta el cambio y genera el UPDATE
        // (detección automática de cambios, o dirty checking).
        List<Pirata> piratas = pirataRepo.findByTripulacionId(origenId);
        for (Pirata pirata : piratas) {
            //pirata.setTripulacion(destino);
            destino.agregarPirata(pirata);
        }

        // Paso 2: la tripulación de destino pasa a dominar los territorios del origen (muchos a muchos)
        for (Territorio territorio : origen.getTerritorios()) {
            destino.agregarTerritorio(territorio);
        }

        // Fallo simulado: los pasos 1 y 2 ya se han hecho EN MEMORIA, pero ningún cambio llega a la
        // base de datos hasta el commit. RuntimeException -> rollback: no cambia nada.
        if (simularFallo) {
            throw new IllegalStateException("Fallo simulado a mitad de la fusión");
        }

        // Paso 3: borrar la tripulación de origen (y sus filas de tripulacion_territorio)
        repo.delete(origen);

        return piratas.size();
    }

    // @Modifying en el repositorio + @Transactional aquí (apartado 10.4)
    @Transactional
    public int subirRecompensa(Long tripulacionId, Long incremento) {
        Tripulacion tripulacion = buscar(tripulacionId);
        return pirataRepo.subirRecompensaTripulacion(tripulacion, incremento);
    }

    // Método derivado de borrado: se une a la transacción del servicio (apartado 10.4)
    @Transactional
    public long eliminarPiratas(Long tripulacionId) {
        return pirataRepo.deleteByTripulacionId(tripulacionId);
    }

    // ═══════════════ AUXILIAR ═══════════════

    private Tripulacion buscar(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la tripulación " + id));
    }
}
