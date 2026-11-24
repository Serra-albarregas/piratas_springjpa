package com.serra.piratas;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.LazyInitializationException;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import com.serra.piratas.entities.FrutaDelDiablo;
import com.serra.piratas.entities.Pirata;
import com.serra.piratas.entities.TipoFruta;
import com.serra.piratas.entities.Territorio;
import com.serra.piratas.entities.Tripulacion;
import com.serra.piratas.repositories.FrutaDelDiabloRepository;
import com.serra.piratas.repositories.PirataRepository;
import com.serra.piratas.repositories.TerritorioRepository;
import com.serra.piratas.repositories.TripulacionRepository;

import lombok.AllArgsConstructor;

/**
 * Demostración de las relaciones (teoría: páginas 8 y 9).
 * Solo se ejecuta con el perfil "relaciones": spring.profiles.active=relaciones
 * Con spring.jpa.show-sql=true, las consultas salen en la consola entre los títulos.
 */
@Component
@Profile("relaciones")
@AllArgsConstructor
public class PruebasRelaciones implements CommandLineRunner {

    PirataRepository pirataRepository;
    TripulacionRepository tripulacionRepository;
    TerritorioRepository territorioRepository;
    FrutaDelDiabloRepository frutaRepository;
    PlatformTransactionManager transactionManager;

    @Override
    public void run(String... args) {
        // Una transacción mantiene la sesión de Hibernate abierta: dentro se pueden cargar datos LAZY.
        TransactionTemplate dentroDeSesion = new TransactionTemplate(transactionManager);

        titulo("1 · N+1: 6 piratas y el nombre de su tripulación (1 consulta + 1 por tripulación distinta)");
        dentroDeSesion.executeWithoutResult(status -> pirataRepository
                .findAll(PageRequest.of(0, 6, Sort.by("nombre")))
                .forEach(p -> System.out.println(p.getNombre() + " -> " + p.getTripulacion().getNombre())));

        titulo("2 · Lo mismo con @EntityGraph: UNA sola consulta con join");
        dentroDeSesion.executeWithoutResult(status -> pirataRepository
                .findByRecompensaGreaterThanEqualOrderByRecompensaDesc(1_000_000_000L)
                .forEach(p -> System.out.println(p.getNombre() + " -> " + p.getTripulacion().getNombre())));

        titulo("3 · LAZY fuera de la sesión: LazyInitializationException");
        Pirata nami = pirataRepository.findByNombreIgnoreCase("Nami").orElseThrow();
        System.out.println(nami); // toString sin relaciones: no hay bucle infinito ni carga perezosa
        try {
            System.out.println(nami.getTripulacion().getNombre());
        } catch (LazyInitializationException e) {
            System.out.println("LazyInitializationException: " + e.getMessage());
        }
        // Con join fetch la tripulación llega ya cargada y no hace falta sesión
        pirataRepository.listarConTripulacion().stream().limit(3)
                .forEach(p -> System.out.println(p.getNombre() + " -> " + p.getTripulacion().getNombre()));

        titulo("4 · MUCHOS A MUCHOS: de la tripulación a sus territorios y de un territorio a sus tripulaciones");
        dentroDeSesion.executeWithoutResult(status -> {
            Tripulacion bigMom = tripulacionRepository.findByNombre("Piratas de Big Mom").orElseThrow();
            System.out.println("Territorios de " + bigMom.getNombre() + ": "
                    + bigMom.getTerritorios().stream().map(Territorio::getNombre).toList());

            Territorio nuevoMundo = territorioRepository.findByNombre("Nuevo Mundo").orElseThrow();
            System.out.println("Tripulaciones en " + nuevoMundo.getNombre() + ": "
                    + nuevoMundo.getTripulaciones().stream().map(Tripulacion::getNombre).toList());
        });

        titulo("5 · Consultas que atraviesan la relación");
        System.out.println("Tripulaciones en Isla Gyojin: " + tripulacionRepository
                .findDistinctByTerritoriosNombre("Isla Gyojin").stream().map(Tripulacion::getNombre).toList());
        System.out.println("Piratas cuya tripulación domina Zou (pirata -> tripulación -> territorios): "
                + pirataRepository.findDistinctByTripulacionTerritoriosNombre("Zou").stream().map(Pirata::getNombre).toList());
        System.out.println("Piratas de los Sombrero de Paja con más de 100 millones: "
                + pirataRepository.buscarMayoresDeUnaTripulacion("Piratas del Sombrero de Paja", 100_000_000L)
                        .stream().map(Pirata::getNombre).toList());

        titulo("6 · agregarTerritorio / quitarTerritorio: solo cuenta el lado propietario (Tripulacion)");
        dentroDeSesion.executeWithoutResult(status -> {
            Tripulacion paja = tripulacionRepository.findByNombre("Piratas del Sombrero de Paja").orElseThrow();
            Territorio dressrosa = territorioRepository.findByNombre("Dressrosa").orElseThrow();

            System.out.println("-- Solo el lado inverso: NO se guarda nada (no hay INSERT)");
            dressrosa.getTripulaciones().add(paja);
            tripulacionRepository.flush();
            dressrosa.getTripulaciones().remove(paja);

            System.out.println("-- Con el método auxiliar (lado propietario): INSERT en tripulacion_territorio");
            paja.agregarTerritorio(dressrosa);
            tripulacionRepository.flush();

            System.out.println("-- quitarTerritorio: DELETE en tripulacion_territorio");
            paja.quitarTerritorio(dressrosa);
            tripulacionRepository.flush();

            status.setRollbackOnly(); // no dejamos rastro en la base de datos
        });

        titulo("7 · UNO A UNO: la fruta del diablo");
        FrutaDelDiablo meraMera = frutaRepository.findByNombre("Mera Mera no Mi").orElseThrow();
        System.out.println("¿Mera Mera no Mi tiene dueño? " + pirataRepository.existsByFruta(meraMera));
        System.out.println("Frutas libres: " + frutaRepository.buscarLibres().stream().map(FrutaDelDiablo::getNombre).toList());
        System.out.println("Piratas con fruta Logia: "
                + pirataRepository.findByFrutaTipo(TipoFruta.LOGIA).stream().map(Pirata::getNombre).toList());

        titulo("8 · equals y hashCode con clave natural");
        Territorio a = territorioRepository.findByNombre("Zou").orElseThrow();
        Territorio b = territorioRepository.findByNombre("Zou").orElseThrow();
        Set<Territorio> conjunto = new HashSet<>(List.of(a, b));
        System.out.println("¿Son el mismo objeto Java (a == b)? " + (a == b));
        System.out.println("¿Son iguales (a.equals(b))? " + a.equals(b));
        System.out.println("Tamaño del Set con los dos: " + conjunto.size());
    }

    private void titulo(String texto) {
        System.out.println("\n==================== " + texto + " ====================");
    }
}
