package com.serra.piratas.dto;

// DTO de SALIDA para el listado de tripulaciones: solo lo que se muestra.
// Se crea directamente desde la consulta (select new), con los recuentos ya calculados.
// No tiene relaciones: no puede lanzar LazyInitializationException ni N+1 consultas.
// En la plantilla, los componentes de un record se leen con paréntesis: ${t.nombre()}
public record TripulacionResumen(Long id, String nombre, String descripcion,
        Integer piratas, Integer territorios) {
}
