package com.serra.piratas.entities;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OrderBy;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Territorio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Include
    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Column(length = 200)
    private String descripcion;

    // Lado INVERSO del muchos a muchos: la tabla intermedia (tripulacion_territorio) la define
    // Tripulacion con @JoinTable. Un territorio puede ser dominado por varias tripulaciones.
    // Se usa Set (no List) para evitar duplicados y borrados/reinserciones innecesarios.
    @ManyToMany(mappedBy = "territorios")
    @OrderBy("nombre")
    @ToString.Exclude
    private Set<Tripulacion> tripulaciones = new HashSet<>();
}
