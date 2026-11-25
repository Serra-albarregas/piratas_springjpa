package com.serra.piratas.services;

import java.io.IOException;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serra.piratas.entities.Pirata;
import com.serra.piratas.entities.Tripulacion;
import com.serra.piratas.repositories.PirataRepository;
import com.serra.piratas.repositories.TripulacionRepository;

import lombok.AllArgsConstructor;

/**
 * SOLO PARA LA DEMOSTRACIÓN (página 10): cada método ilustra una regla de @Transactional.
 * Solo existe con el perfil "transacciones" y lo usa PruebasTransacciones.
 * Esta clase NO lleva @Transactional a nivel de clase: cada método indica lo suyo.
 */
@Service
@Profile("transacciones")
@AllArgsConstructor
public class DemoTransaccionesService {

    private final PirataRepository pirataRepository;
    private final TripulacionRepository tripulacionRepository;

    // ── Detección automática de cambios: se cambia la entidad y NO se llama a save ──
    @Transactional
    public void cambioSinSave(String nombre) {
        Pirata pirata = pirataRepository.findByNombreIgnoreCase(nombre).orElseThrow();
        pirata.setRecompensa(pirata.getRecompensa() + 1_000);
        // sin save: al hacer commit, Hibernate detecta el cambio y lanza el UPDATE
    }

    // ── Lo mismo en una transacción de solo lectura: el cambio se IGNORA ──
    @Transactional(readOnly = true)
    public void cambioEnSoloLectura(String nombre) {
        Pirata pirata = pirataRepository.findByNombreIgnoreCase(nombre).orElseThrow();
        pirata.setRecompensa(pirata.getRecompensa() + 1_000);
        // readOnly: no hay UPDATE
    }

    // ── Rollback: excepción NO comprobada (RuntimeException) -> se deshace todo ──
    @Transactional
    public void cambioYFalloNoComprobado(String nombre) {
        Pirata pirata = pirataRepository.findByNombreIgnoreCase(nombre).orElseThrow();
        pirata.setRecompensa(pirata.getRecompensa() + 1_000);
        throw new IllegalStateException("Fallo simulado (excepción no comprobada)");
    }

    // ── Excepción COMPROBADA (IOException): por defecto la transacción se CONFIRMA igualmente ──
    @Transactional
    public void cambioYFalloComprobado(String nombre) throws IOException {
        Pirata pirata = pirataRepository.findByNombreIgnoreCase(nombre).orElseThrow();
        pirata.setRecompensa(pirata.getRecompensa() + 1_000);
        throw new IOException("Fallo simulado (excepción comprobada)");
    }

    // ── Con rollbackFor = Exception.class, la excepción comprobada también deshace los cambios ──
    @Transactional(rollbackFor = Exception.class)
    public void cambioYFalloComprobadoConRollback(String nombre) throws IOException {
        Pirata pirata = pirataRepository.findByNombreIgnoreCase(nombre).orElseThrow();
        pirata.setRecompensa(pirata.getRecompensa() + 1_000);
        throw new IOException("Fallo simulado (excepción comprobada, con rollbackFor)");
    }

    // ── Cuándo @Transactional NO funciona: llamada a un método de la MISMA clase ──
    // Este método no es transaccional y llama a otro de this: la llamada no pasa por el proxy,
    // así que la anotación del segundo se ignora y el @Modifying falla por no haber transacción.
    public int llamadaInterna(Long tripulacionId) {
        return subirRecompensaTransaccional(tripulacionId);
    }

    // Llamado DESDE OTRA CLASE (por el proxy), sí abre la transacción y funciona
    @Transactional
    public int subirRecompensaTransaccional(Long tripulacionId) {
        Tripulacion tripulacion = tripulacionRepository.findById(tripulacionId).orElseThrow();
        return pirataRepository.subirRecompensaTripulacion(tripulacion, 1L);
    }

    // ── LazyInitializationException: la colección se usa FUERA de la transacción ──
    // El repositorio abre y cierra su transacción dentro de findByNombre; al llegar a la
    // siguiente línea la tripulación ya está separada y su lista de piratas no se había cargado.
    public int contarPiratasSinTransaccion(String tripulacion) {
        Tripulacion t = tripulacionRepository.findByNombre(tripulacion).orElseThrow();
        return t.getPiratas().size();
    }

    // ── La solución: trabajar dentro de una transacción ──
    @Transactional(readOnly = true)
    public int contarPiratasConTransaccion(String tripulacion) {
        Tripulacion t = tripulacionRepository.findByNombre(tripulacion).orElseThrow();
        return t.getPiratas().size();
    }
}
