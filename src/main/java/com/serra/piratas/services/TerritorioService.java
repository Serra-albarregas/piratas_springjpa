package com.serra.piratas.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serra.piratas.entities.Territorio;
import com.serra.piratas.exceptions.RecursoNoEncontradoException;
import com.serra.piratas.repositories.TerritorioRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class TerritorioService {

    private final TerritorioRepository repo;

    // Con join fetch: las tripulaciones llegan cargadas y la plantilla no necesita la conexión abierta
    public List<Territorio> listarConTripulaciones() {
        return repo.listarConTripulaciones();
    }

    public Territorio obtener(Long id) {
        return repo.buscarConTripulaciones(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el territorio " + id));
    }
}
