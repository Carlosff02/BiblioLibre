package com.example.biblo.service;

import com.example.biblo.models.PaginasGuardadas;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PaginasGuardadasRepository extends JpaRepository<PaginasGuardadas, Long> {
    @EntityGraph(attributePaths = {
            "libros",
            "libros.autor"
    })
    Optional<PaginasGuardadas> findByIdiomaAndNumeroPagina(String idioma, Integer numeroPagina);

    @Query("""
    SELECT DISTINCT p
    FROM PaginasGuardadas p
    LEFT JOIN FETCH p.libros l
    LEFT JOIN FETCH l.autor a
    WHERE p.idioma = :idioma
    AND p.numeroPagina = :page
    """)
    Optional<PaginasGuardadas> findByIdiomaAndNumeroPaginaConLibrosYAutores(
            @Param("idioma") String idioma,
            @Param("page") Integer page);


}
