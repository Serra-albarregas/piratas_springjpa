package com.serra.piratas.services;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.serra.piratas.entities.FrutaDelDiablo;
import com.serra.piratas.repositories.FrutaDelDiabloRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class FrutaDelDiabloService {

    private final FrutaDelDiabloRepository repo;

    // Frutas sin dueño: las únicas que se pueden asignar a un pirata nuevo
    public List<FrutaDelDiablo> listarLibres() {
        return repo.buscarLibres();
    }
}
