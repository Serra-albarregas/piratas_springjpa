package com.serra.piratas;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import com.serra.piratas.entities.Pirata;
import com.serra.piratas.repositories.PirataRepository;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor
public class Pruebas {
    PirataRepository pirataRepository;

    @PostConstruct
    public void pruebasRepo() {
        System.out.println("===========PRUEBAS===========");

        pirataRepository
                .filtrar(null, "sombrero de paja", null,
                        PageRequest.of(0, 5, Sort.by("recompensa").ascending()
                        .and(Sort.by("nombre").descending())))
                .forEach(System.out::println);
    }
}
