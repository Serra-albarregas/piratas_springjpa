package com.serra.piratas.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.serra.piratas.entities.Territorio;

public interface TerritorioRepository extends JpaRepository<Territorio, Long> {

        Optional<Territorio> findByNombre(String nombre);

        List<Territorio> findAllByOrderByNombreAsc();

        // Lado inverso del muchos a muchos: territorios con sus tripulaciones en una sola consulta
        @Query("select distinct te from Territorio te left join fetch te.tripulaciones order by te.nombre")
        List<Territorio> listarConTripulaciones();

        // Ficha de un territorio con sus tripulaciones ya cargadas (necesario con open-in-view=false)
        @Query("select te from Territorio te left join fetch te.tripulaciones where te.id = :id")
        Optional<Territorio> buscarConTripulaciones(@Param("id") Long id);
}
