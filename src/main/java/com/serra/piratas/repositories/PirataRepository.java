package com.serra.piratas.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.serra.piratas.dto.PirataResumen;
import com.serra.piratas.dto.ResumenTripulacion;
import com.serra.piratas.entities.Pirata;

public interface PirataRepository extends JpaRepository<Pirata, Long> {

        // region Métodos derivados
        // 1.1 Buscar por nombre exacto
        List<Pirata> findByNombreEquals(String nombre);

        // 1.2 Buscar piratas cuya recompensa sea menor o igual
        // a una cantidad
        List<Pirata> findByRecompensaLessThanEqual(Long recompensa);

        // 1.3 Buscar por tripulación ignorando mayúsculas
        List<Pirata> findByTripulacionIgnoreCase(String tripulacion);

        // 1.4 Buscar piratas cuyo nombre termine en una cadena
        List<Pirata> findByNombreEndingWith(String cadena);

        // 1.5 Buscar piratas cuya descripción no contenga
        // una palabra determinada
        List<Pirata> findByDescripcionNotLike(String palabra);

        // 1.6 Buscar piratas cuya fruta del diablo sea distinta
        // de una determinada
        List<Pirata> findByFrutaDelDiabloNot(String frutaDelDiablo);

        // 1.7 Buscar piratas con recompensa distinta de una cantidad
        List<Pirata> findByRecompensaNot(Long recompensa);

        // 1.8 Buscar por nombre exacto y tripulación
        // ordenando por nombre alfabéticamente
        List<Pirata> findByNombreAndTripulacionOrderByNombreDesc(String nombre, String tripulacion);

        // 1.9 Obtener los dos primeros piratas de una tripulación
        // ordenados por nombre descendente
        List<Pirata> findFirst2ByTripulacionOrderByNombreDesc(String tripulacion);

        // 1.10 Obtener un pirata por su nombre, ignorando mayúsculas
        Optional<Pirata> findByNombreIgnoreCase(String nombre);

        // 1.11 Contar piratas cuya recompensa sea mayor o igual
        // a una cantidad
        Long countByRecompensaGreaterThanEqual(Long recompensa);

        // 1.12 Comprobar si existen piratas con una fruta determinada
        boolean existsByFrutaDelDiablo(String frutaDelDiablo);

        // 1.13 Buscar los piratas sin fruta del diablo
        List<Pirata> findByFrutaDelDiabloIsNull();

        // endregion

        // region query

        // ── 2.1 JPQL con parámetros con nombre (equivale a findByRecompensaGreaterThan
        // + OrderBy) ──
        @Query("SELECT p FROM Pirata p WHERE p.recompensa > :min ORDER BY p.recompensa DESC")
        List<Pirata> buscarPorRecompensaMayorQue(@Param("min") Long min);

        // ── 2.2 Parámetros posicionales (?1, ?2): no necesitan @Param ──
        @Query("SELECT p FROM Pirata p WHERE p.tripulacion = ?1 AND p.recompensa >= ?2")
        List<Pirata> buscarPorTripulacionYRecompensa(String tripulacion, Long min);

        // ── 2.3 LIKE con CONCAT, varios campos y text block ──
        @Query("""
                SELECT p FROM Pirata p
                WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :texto, '%'))
                        OR LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :texto, '%'))
                ORDER BY p.nombre
                """)
        List<Pirata> buscarPorTexto(@Param("texto") String texto);

        // ── 2.4 IN, BETWEEN e IS NOT NULL ──
        @Query("SELECT p FROM Pirata p WHERE p.tripulacion IN (:tripulaciones) ORDER BY p.nombre")
        List<Pirata> buscarEnTripulaciones(@Param("tripulaciones") List<String> tripulaciones);

        @Query("""
                SELECT p FROM Pirata p
                WHERE p.recompensa BETWEEN :min AND :max
                        AND p.frutaDelDiablo IS NOT NULL
                """)
        List<Pirata> buscarConFrutaEnRango(@Param("min") Long min, @Param("max") Long max);

        // ── 2.5 Valores escalares y funciones de agregado ──
        @Query("SELECT DISTINCT p.tripulacion FROM Pirata p ORDER BY p.tripulacion")
        List<String> listarTripulaciones();

        @Query("SELECT COALESCE(SUM(p.recompensa), 0L) FROM Pirata p")
        Long sumaTotalRecompensas();

        @Query("SELECT COALESCE(SUM(p.recompensa), 0L) FROM Pirata p WHERE p.tripulacion = :tripulacion")
        Long sumaRecompensasDe(@Param("tripulacion") String tripulacion);

        @Query("SELECT AVG(p.recompensa) FROM Pirata p")
        Double recompensaMedia(); // null si la tabla está vacía

        @Query("SELECT MAX(p.recompensa) FROM Pirata p")
        Optional<Long> recompensaMaxima();

        // ── 2.6 GROUP BY / HAVING con proyección de interfaz ──
        @Query("""
                SELECT p.tripulacion AS tripulacion,
                        COUNT(p)      AS miembros,
                        SUM(p.recompensa) AS recompensaTotal
                FROM Pirata p
                GROUP BY p.tripulacion
                ORDER BY SUM(p.recompensa) DESC
                """)
        List<ResumenTripulacion> resumenPorTripulacion();

        @Query("""
                SELECT p.tripulacion AS tripulacion,
                        COUNT(p)      AS miembros,
                        SUM(p.recompensa) AS recompensaTotal
                FROM Pirata p
                GROUP BY p.tripulacion
                HAVING COUNT(p) >= :minimo
                ORDER BY COUNT(p) DESC
                """)
        List<ResumenTripulacion> resumenTripulacionesConAlMenos(@Param("minimo") long minimo);

        // ── 2.7 Proyección a DTO (constructor expression): solo carga las columnas
        // necesarias ──
        @Query("""
                SELECT new com.serra.piratas.dto.PirataResumen(p.id, p.nombre, p.recompensa)
                FROM Pirata p
                ORDER BY p.recompensa DESC
                """)
        List<PirataResumen> listarResumenes();

        // ── 2.8 Modificaciones: @Modifying + @Transactional ──
        @Transactional
        @Modifying(clearAutomatically = true, flushAutomatically = true)
        @Query("UPDATE Pirata p SET p.recompensa = p.recompensa + :incremento WHERE p.tripulacion = :tripulacion")
        int subirRecompensaTripulacion(@Param("tripulacion") String tripulacion,
                        @Param("incremento") Long incremento);

        @Transactional
        @Modifying
        @Query("DELETE FROM Pirata p WHERE p.recompensa < :min")
        int eliminarConRecompensaMenorQue(@Param("min") Long min);

        // ── 2.9 EL FILTRO OPTATIVO: 3 filtros opcionales en UNA consulta + paginación
        // Si el parámetro es null, su condición se anula.
        @Query("""
                SELECT p FROM Pirata p
                WHERE (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
                        AND (:tripulacion IS NULL OR LOWER(p.tripulacion) = LOWER(:tripulacion))
                        AND (:recompensaMin IS NULL OR p.recompensa >= :recompensaMin)
                """)
        Page<Pirata> filtrar(@Param("nombre") String nombre,
                        @Param("tripulacion") String tripulacion,
                        @Param("recompensaMin") Long recompensaMin,
                        Pageable pageable);

        // 3 · PAGINACIÓN

        // findAll(Pageable) ya viene en JpaRepository.

        // ── 3.1 Derivada que devuelve Page (hace SELECT + SELECT COUNT) ──
        Page<Pirata> findByTripulacionIgnoreCase(String tripulacion, Pageable pageable);

        // ── 3.2 Slice: sin COUNT. Solo sabe si hay página siguiente (scroll infinito, cargar más) ──
        Slice<Pirata> findByFrutaDelDiabloIsNotNull(Pageable pageable);

        // ── 3.3 List + Pageable: solo aplica LIMIT/OFFSET y orden, no devuelve metadatos ──
        List<Pirata> findByRecompensaGreaterThan(Long recompensa, Pageable pageable);

        // ── 3.4 @Query con proyección a DTO paginada ──
        @Query("SELECT new com.serra.piratas.dto.PirataResumen(p.id, p.nombre, p.recompensa) FROM Pirata p")
        Page<PirataResumen> paginaResumenes(Pageable pageable);

        // endregion
}