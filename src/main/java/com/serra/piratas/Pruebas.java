package com.serra.piratas;

import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.serra.piratas.repositories.PirataRepository;

import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class Pruebas implements CommandLineRunner {

    private final PirataRepository pirataRepository;

    @Override
    @Transactional
    public void run(String... args) {

        System.out.println("===========PRUEBAS===========");

        pirataRepository
                .filtrar(null, 1L, null,
                    PageRequest.of(0, 5,
                    Sort.by("recompensa").ascending().and(Sort.by("nombre").descending())))
                .forEach(System.out::println);

        pirataRepository
                .buscarPorNombreDeTripulacion("Piratas Donquixote")
                .forEach(p -> System.out.println(
                        p.getFruta()));
    }
}
