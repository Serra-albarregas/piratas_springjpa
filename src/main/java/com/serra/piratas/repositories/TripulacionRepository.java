package com.serra.piratas.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.serra.piratas.dto.TripulacionResumen;
import com.serra.piratas.entities.Tripulacion;

public interface TripulacionRepository extends JpaRepository<Tripulacion, Long> {

        Optional<Tripulacion> findByNombre(String nombre);

        List<Tripulacion> findAllByOrderByNombreAsc();

        // ── Muchos a muchos: tripulaciones que dominan un territorio ──
        List<Tripulacion> findDistinctByTerritoriosNombre(String territorio);

        @Query("""
                select distinct t from Tripulacion t
                join t.territorios te
                where te.nombre = :territorio
                order by t.nombre
                """)
        List<Tripulacion> buscarPorTerritorio(@Param("territorio") String territorio);

        // ── N+1 con colecciones: sin esto, mostrar los territorios de cada tripulación en un listado
        // lanza una consulta extra POR CADA tripulación. Con join fetch se piden todos a la vez.
        // (En Hibernate 6+ las filas ya no se duplican; distinct es opcional aquí.)
        @Query("select distinct t from Tripulacion t left join fetch t.territorios order by t.nombre")
        List<Tripulacion> listarConTerritorios();

        // Lo mismo con @EntityGraph, sin escribir JPQL
        @EntityGraph(attributePaths = { "territorios" })
        List<Tripulacion> findDistinctByOrderByNombreAsc();

        // ── DTO de salida con los recuentos calculados en la propia consulta (página 10):
        // una sola consulta para todo el listado, sin cargar colecciones.
        @Query("""
                select new com.serra.piratas.dto.TripulacionResumen(
                        t.id, t.nombre, t.descripcion, size(t.piratas), size(t.territorios))
                from Tripulacion t
                order by t.nombre
                """)
        List<TripulacionResumen> listarResumen();
}
