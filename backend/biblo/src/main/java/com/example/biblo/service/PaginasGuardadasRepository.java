package com.example.biblo.service;

import com.example.biblo.models.PaginasGuardadas;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaginasGuardadasRepository extends JpaRepository<PaginasGuardadas, Long> {
    Optional<PaginasGuardadas> findByIdiomaAndNumeroPagina(String idioma, Integer page);
}
