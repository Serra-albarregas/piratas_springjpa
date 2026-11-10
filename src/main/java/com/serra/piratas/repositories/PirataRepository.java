package com.serra.piratas.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import com.serra.piratas.entities.Pirata;

public interface PirataRepository extends JpaRepository<Pirata, Long> {

}