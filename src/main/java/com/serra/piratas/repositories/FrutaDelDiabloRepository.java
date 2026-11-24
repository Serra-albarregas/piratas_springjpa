package com.serra.piratas.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.serra.piratas.entities.FrutaDelDiablo;
import com.serra.piratas.entities.TipoFruta;

public interface FrutaDelDiabloRepository extends JpaRepository<FrutaDelDiablo, Long> {

        Optional<FrutaDelDiablo> findByNombre(String nombre);

        List<FrutaDelDiablo> findByTipoOrderByNombreAsc(TipoFruta tipo);

        // Frutas sin dueño: las únicas que se pueden asignar a un pirata nuevo,
        // porque la columna fruta_id de Pirata es única (relación uno a uno)
        @Query("""
                SELECT f FROM FrutaDelDiablo f
                WHERE NOT EXISTS (SELECT p.id FROM Pirata p WHERE p.fruta = f)
                ORDER BY f.nombre
                """)
        List<FrutaDelDiablo> buscarLibres();
}
