package com.example.biblo.repository;

import com.example.biblo.models.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LibroRepository extends JpaRepository<Libro, Long> {
    List<Libro> findByIdioma(String idioma);

    Optional<Libro> findByTitulo(String titulo);

    @Query(value = """
            SELECT * FROM libro
            ORDER BY descargas DESC LIMIT 10
            """, nativeQuery = true)
    List<Libro> buscarLibrosPopulares();

    Optional<Libro> findFirstByTituloContainingIgnoreCase(String titulo);
}
