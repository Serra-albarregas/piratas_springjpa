package com.serra.piratas.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

// Relación UNIDIRECCIONAL con Pirata: la fruta no sabe quién la tiene.
// El lado propietario es Pirata (columna fruta_id, con unique = true).
// A diferencia del carné de biblioteca de la teoría, la fruta existe por sí sola
// (si su dueño muere, pasa a otro), por eso Pirata NO usa cascade ni orphanRemoval.
@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class FrutaDelDiablo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true, length = 80)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoFruta tipo;
}
