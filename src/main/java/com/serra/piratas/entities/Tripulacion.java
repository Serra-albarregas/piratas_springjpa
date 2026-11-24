package com.serra.piratas.entities;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Data
@NoArgsConstructor
// equals y hashCode solo con la clave natural (el nombre): con @Data, por defecto,
// intentarían comparar también la lista de piratas, que a su vez apunta a esta tripulación...
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tripulacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @EqualsAndHashCode.Include
    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 60, message = "Máximo 60 caracteres")
    @Column(nullable = false, unique = true, length = 60)
    private String nombre;

    @Size(max = 200, message = "Máximo 200 caracteres")
    @Column(length = 200)
    private String descripcion;

    // Lado INVERSO: no crea ninguna columna, solo permite navegar de la tripulación a sus piratas.
    // La clave foránea (tripulacion_id) está en la tabla pirata, en el lado propietario.
    // @ToString.Exclude evita el bucle infinito Tripulacion.toString -> Pirata.toString -> ...
    @OneToMany(mappedBy = "tripulacion")
    @OrderBy("recompensa DESC, nombre ASC")
    @ToString.Exclude
    private List<Pirata> piratas = new ArrayList<>();

    // ── MUCHOS A MUCHOS: una tripulación domina varios territorios y un territorio es dominado
    // por varias tripulaciones. Lado PROPIETARIO: define la tabla intermedia tripulacion_territorio.
    @ManyToMany
    @JoinTable(name = "tripulacion_territorio",
            joinColumns = @JoinColumn(name = "tripulacion_id"),
            inverseJoinColumns = @JoinColumn(name = "territorio_id"))
    @OrderBy("nombre")
    @ToString.Exclude
    private Set<Territorio> territorios = new HashSet<>();

    // Métodos auxiliares: mantienen sincronizados los dos lados de cada relación.
    // Lo que realmente se guarda es pirata.setTripulacion(...) y tripulacion.getTerritorios(): los lados propietarios.
    public void agregarPirata(Pirata pirata) {
        piratas.add(pirata);
        pirata.setTripulacion(this);
    }

    public void quitarPirata(Pirata pirata) {
        piratas.remove(pirata);
        pirata.setTripulacion(null);
    }

    public void agregarTerritorio(Territorio territorio) {
        territorios.add(territorio);
        territorio.getTripulaciones().add(this);
    }

    public void quitarTerritorio(Territorio territorio) {
        territorios.remove(territorio);
        territorio.getTripulaciones().remove(this);
    }
}
