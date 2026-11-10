package com.serra.piratas;

import org.springframework.stereotype.Component;

import com.serra.piratas.repositories.PirataRepository;

import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;

@Component
@AllArgsConstructor 
public class Pruebas {
    PirataRepository pirataRepository;
    
    @PostConstruct
    public void pruebasRepo(){
        System.out.println("===========PRUEBAS===========");

    }
}
