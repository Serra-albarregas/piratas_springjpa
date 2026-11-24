package com.serra.piratas.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// Con @Data, equals y hashCode usarían TODOS los campos, relaciones incluidas (y forzarían la carga
// perezosa de la tripulación y la fruta). Se limitan a la clave natural: el nombre (por eso es unique).
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Pirata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Include
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 60, message = "El nombre debe tener entre 2 y 60 caracteres")
    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    // ── UNO A MUCHOS: muchos piratas pertenecen a una tripulación ──
    // Lado PROPIETARIO: aquí está la clave foránea (tripulacion_id).
    // LAZY: la tripulación se carga solo cuando se usa (se evita el N+1 con @EntityGraph o join fetch).
    @NotNull(message = "Debes elegir una tripulación")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tripulacion_id", nullable = false)
    @ToString.Exclude
    private Tripulacion tripulacion;

    // ── UNO A UNO: un pirata tiene como máximo una fruta, y una fruta solo tiene un dueño ──
    // unique = true es lo que convierte la relación en "uno a uno".
    // Sin cascade: la fruta se crea por separado y el pirata solo la referencia.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fruta_id", unique = true)
    @ToString.Exclude
    private FrutaDelDiablo fruta;

    @NotNull(message = "La recompensa es obligatoria")
    @PositiveOrZero(message = "La recompensa no puede ser negativa")
    @Column(nullable = false)
    private Long recompensa;

    @Size(max = 500, message = "Máximo 500 caracteres")
    @Column(length = 500)
    private String descripcion;
}
