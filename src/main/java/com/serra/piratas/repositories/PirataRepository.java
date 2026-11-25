package com.serra.piratas.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.serra.piratas.dto.PirataResumen;
import com.serra.piratas.dto.ResumenTripulacion;
import com.serra.piratas.entities.FrutaDelDiablo;
import com.serra.piratas.entities.Pirata;
import com.serra.piratas.entities.Tripulacion;
import com.serra.piratas.entities.TipoFruta;

public interface PirataRepository extends JpaRepository<Pirata, Long> {

        // region Métodos derivados
        // 1.1 Buscar por nombre exacto
        List<Pirata> findByNombreEquals(String nombre);

        // 1.2 Buscar piratas cuya recompensa sea menor o igual
        // a una cantidad
        List<Pirata> findByRecompensaLessThanEqual(Long recompensa);

        // 1.3 Buscar por nombre de tripulación ignorando mayúsculas
        // (recorre la relación: p.tripulacion.nombre)
        List<Pirata> findByTripulacionNombreIgnoreCase(String tripulacion);

        // 1.4 Buscar piratas cuyo nombre termine en una cadena
        List<Pirata> findByNombreEndingWith(String cadena);

        // 1.5 Buscar piratas cuya descripción no contenga
        // una palabra determinada
        List<Pirata> findByDescripcionNotLike(String palabra);

        // 1.6 Buscar piratas cuya fruta del diablo sea distinta
        // de una determinada (por nombre de fruta)
        List<Pirata> findByFrutaNombreNot(String fruta);

        // 1.7 Buscar piratas con recompensa distinta de una cantidad
        List<Pirata> findByRecompensaNot(Long recompensa);

        // 1.8 Buscar por nombre exacto y tripulación
        // ordenando por nombre alfabéticamente
        List<Pirata> findByNombreAndTripulacionNombreOrderByNombreDesc(String nombre, String tripulacion);

        // 1.9 Obtener los dos primeros piratas de una tripulación
        // ordenados por nombre descendente
        List<Pirata> findFirst2ByTripulacionNombreOrderByNombreDesc(String tripulacion);

        // 1.10 Obtener un pirata por su nombre, ignorando mayúsculas
        Optional<Pirata> findByNombreIgnoreCase(String nombre);

        // 1.11 Contar piratas cuya recompensa sea mayor o igual
        // a una cantidad
        Long countByRecompensaGreaterThanEqual(Long recompensa);

        // 1.12 Comprobar si existen piratas con una fruta determinada (por nombre)
        boolean existsByFrutaNombre(String fruta);

        // 1.13 Buscar los piratas sin fruta del diablo
        List<Pirata> findByFrutaIsNull();

        // endregion

        // region query

        // ── 2.1 JPQL con parámetros con nombre (equivale a findByRecompensaGreaterThan
        // + OrderBy) ──
        @Query("SELECT p FROM Pirata p WHERE p.recompensa > :min ORDER BY p.recompensa DESC")
        List<Pirata> buscarPorRecompensaMayorQue(@Param("min") Long min);

        // ── 2.2 Parámetros posicionales (?1, ?2): no necesitan @Param ──
        @Query("SELECT p FROM Pirata p WHERE p.tripulacion.nombre = ?1 AND p.recompensa >= ?2")
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
        @Query("SELECT p FROM Pirata p WHERE p.tripulacion.nombre IN (:tripulaciones) ORDER BY p.nombre")
        List<Pirata> buscarEnTripulaciones(@Param("tripulaciones") List<String> tripulaciones);

        @Query("""
                SELECT p FROM Pirata p
                WHERE p.recompensa BETWEEN :min AND :max
                        AND p.fruta IS NOT NULL
                """)
        List<Pirata> buscarConFrutaEnRango(@Param("min") Long min, @Param("max") Long max);

        // ── 2.5 Valores escalares y funciones de agregado ──
        // (listarTripulaciones() ya no existe aquí: ahora es TripulacionRepository.findAllByOrderByNombreAsc())

        @Query("SELECT COALESCE(SUM(p.recompensa), 0L) FROM Pirata p")
        Long sumaTotalRecompensas();

        @Query("SELECT COALESCE(SUM(p.recompensa), 0L) FROM Pirata p WHERE p.tripulacion.nombre = :tripulacion")
        Long sumaRecompensasDe(@Param("tripulacion") String tripulacion);

        @Query("SELECT AVG(p.recompensa) FROM Pirata p")
        Double recompensaMedia(); // null si la tabla está vacía

        @Query("SELECT MAX(p.recompensa) FROM Pirata p")
        Optional<Long> recompensaMaxima();

        // ── 2.6 GROUP BY / HAVING con proyección de interfaz ──
        @Query("""
                SELECT p.tripulacion.nombre AS tripulacion,
                        COUNT(p)      AS miembros,
                        SUM(p.recompensa) AS recompensa
                FROM Pirata p
                GROUP BY p.tripulacion.nombre
                ORDER BY SUM(p.recompensa) DESC
                """)
        List<ResumenTripulacion> resumenPorTripulacion();

        @Query("""
                SELECT p.tripulacion.nombre AS tripulacion,
                        COUNT(p)      AS miembros,
                        SUM(p.recompensa) AS recompensa
                FROM Pirata p
                GROUP BY p.tripulacion.nombre
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

        // ── 2.8 Modificaciones: @Modifying ──
        // Ya NO llevan @Transactional (página 10): la transacción se abre en el servicio.
        // Llamados directamente, sin transacción, lanzan TransactionRequiredException.
        @Modifying(clearAutomatically = true, flushAutomatically = true)
        // Se compara con la ENTIDAD (p.tripulacion = :tripulacion), sin recorrer la relación dentro del UPDATE
        @Query("UPDATE Pirata p SET p.recompensa = p.recompensa + :incremento WHERE p.tripulacion = :tripulacion")
        int subirRecompensaTripulacion(@Param("tripulacion") Tripulacion tripulacion,
                        @Param("incremento") Long incremento);

        @Modifying
        @Query("DELETE FROM Pirata p WHERE p.recompensa < :min")
        int eliminarConRecompensaMenorQue(@Param("min") Long min);

        // ── 2.9 EL FILTRO OPTATIVO: 3 filtros opcionales en UNA consulta + paginación
        // Si el parámetro es null, su condición se anula.
        // La tripulación se filtra por id (p.tripulacion.id es la clave foránea: no necesita join).
        // @EntityGraph carga la tripulación y la fruta en la MISMA consulta: sin él, la lista haría
        // una consulta extra por cada pirata (problema N+1). Quítalo y mira la consola (show-sql).
        @EntityGraph(attributePaths = { "tripulacion", "fruta" })
        @Query("""
                SELECT p FROM Pirata p
                WHERE (:nombre IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')))
                        AND (:tripulacionId IS NULL OR p.tripulacion.id = :tripulacionId)
                        AND (:recompensaMin IS NULL OR p.recompensa >= :recompensaMin)
                """)
        Page<Pirata> filtrar(@Param("nombre") String nombre,
                        @Param("tripulacionId") Long tripulacionId,
                        @Param("recompensaMin") Long recompensaMin,
                        Pageable pageable);

        // 3 · PAGINACIÓN

        // findAll(Pageable) ya viene en JpaRepository.

        // ── 3.1 Derivada que devuelve Page (hace SELECT + SELECT COUNT) ──
        Page<Pirata> findByTripulacionNombreIgnoreCase(String tripulacion, Pageable pageable);

        // ── 3.2 Slice: sin COUNT. Solo sabe si hay página siguiente (scroll infinito, cargar más) ──
        Slice<Pirata> findByFrutaIsNotNull(Pageable pageable);

        // ── 3.3 List + Pageable: solo aplica LIMIT/OFFSET y orden, no devuelve metadatos ──
        List<Pirata> findByRecompensaGreaterThan(Long recompensa, Pageable pageable);

        // ── 3.4 @Query con proyección a DTO paginada ──
        @Query("SELECT new com.serra.piratas.dto.PirataResumen(p.id, p.nombre, p.recompensa) FROM Pirata p")
        Page<PirataResumen> paginaResumenes(Pageable pageable);

        // endregion

        // region relaciones (teoría: páginas 8 y 9)

        // ── R1. UNO A MUCHOS: derivadas que atraviesan la relación @ManyToOne ──
        // findByTripulacionNombre se interpreta como p.tripulacion.nombre
        List<Pirata> findByTripulacionNombre(String nombre);

        // Por el objeto o por su id (el id es la clave foránea: no hace falta join)
        List<Pirata> findByTripulacion(Tripulacion tripulacion);

        List<Pirata> findByTripulacionId(Long tripulacionId);

        long countByTripulacionId(Long tripulacionId);

        // ── R2. JPQL: navegar con el punto o enlazar explícitamente con join ──
        @Query("select p from Pirata p where p.tripulacion.nombre = :nombre order by p.nombre")
        List<Pirata> buscarPorNombreDeTripulacion(@Param("nombre") String nombre);

        // El join se escribe sobre el ATRIBUTO de la relación (join p.tripulacion t), sin "on"
        @Query("""
                select p from Pirata p
                join p.tripulacion t
                where t.nombre = :nombre and p.recompensa >= :recompensa
                """)
        List<Pirata> buscarMayoresDeUnaTripulacion(@Param("nombre") String nombre,
                        @Param("recompensa") Long recompensa);

        // ── R3. UNO A UNO: la fruta del diablo ──
        boolean existsByFruta(FrutaDelDiablo fruta);

        boolean existsByFrutaId(Long frutaId);

        List<Pirata> findByFrutaTipo(TipoFruta tipo);

        @Query("select p from Pirata p join p.fruta f where f.tipo = :tipo order by p.recompensa desc")
        List<Pirata> buscarPorTipoDeFruta(@Param("tipo") TipoFruta tipo);

        // ── R4. MUCHOS A MUCHOS, atravesando dos relaciones: pirata -> tripulación -> territorios ──
        // distinct evita repetir un pirata si su tripulación aparece por varias filas
        List<Pirata> findDistinctByTripulacionTerritoriosNombre(String territorio);

        @Query("""
                select distinct p from Pirata p
                join p.tripulacion t
                join t.territorios te
                where te.nombre = :territorio
                order by p.nombre
                """)
        List<Pirata> buscarPorTerritorio(@Param("territorio") String territorio);

        // ── R6. Servicios y transacciones (página 10) ──
        // Derivada de borrado: necesita una transacción activa (la abre el servicio).
        // Borra uno a uno los piratas encontrados y devuelve cuántos ha borrado.
        long deleteByTripulacionId(Long tripulacionId);

        // Ficha completa de un pirata en UNA consulta: tripulación, territorios de su tripulación y fruta.
        // Con open-in-view=false, la plantilla no puede cargar nada perezosamente:
        // el servicio debe devolver todo lo que la vista necesita ya cargado.
        @Query("""
                select p from Pirata p
                join fetch p.tripulacion t
                left join fetch t.territorios
                left join fetch p.fruta
                where p.id = :id
                """)
        Optional<Pirata> buscarConDetalle(@Param("id") Long id);

        // ── R5. N+1: pedir la relación DENTRO de la misma consulta ──
        @Query("select p from Pirata p join fetch p.tripulacion order by p.nombre")
        List<Pirata> listarConTripulacion();

        @EntityGraph(attributePaths = { "tripulacion" })
        List<Pirata> findByRecompensaGreaterThanEqualOrderByRecompensaDesc(Long recompensa);

        // endregion
}
