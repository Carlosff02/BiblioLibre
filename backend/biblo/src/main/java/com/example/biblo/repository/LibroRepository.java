package com.example.biblo.repository;

import com.example.biblo.models.Libro;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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


    // 1️⃣ Buscar solo por idioma
    @Query("""
        SELECT DISTINCT l FROM Libro l
        LEFT JOIN FETCH l.autor a
        WHERE LOWER(l.idioma) = LOWER(:idioma)
    """)
    Optional<List<Libro>> buscarPorIdioma(@Param("idioma") String idioma);

    // 2️⃣ Buscar por idioma y autor
    @Query("""
        SELECT DISTINCT l FROM Libro l
        LEFT JOIN FETCH l.autor a
        WHERE LOWER(l.idioma) = LOWER(:idioma)
          AND LOWER(a.nombre) LIKE LOWER(CONCAT('%', :autor, '%'))
    """)
    Optional<List<Libro>> buscarPorIdiomaYAutor(
            @Param("idioma") String idioma,
            @Param("autor") String autor);

    // 3️⃣ Buscar por idioma y título
    @Query("""
        SELECT DISTINCT l FROM Libro l
        LEFT JOIN FETCH l.autor a
        WHERE LOWER(l.idioma) = LOWER(:idioma)
          AND LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))
    """)
    Optional<List<Libro>> buscarPorIdiomaYTitulo(
            @Param("idioma") String idioma,
            @Param("titulo") String titulo);

    // 4️⃣ Buscar por idioma, autor y título
    @Query("""
        SELECT DISTINCT l FROM Libro l
        LEFT JOIN FETCH l.autor a
        WHERE LOWER(l.idioma) = LOWER(:idioma)
          AND LOWER(l.titulo) LIKE LOWER(CONCAT('%', :titulo, '%'))
          AND LOWER(a.nombre) LIKE LOWER(CONCAT('%', :autor, '%'))
    """)
    Optional<List<Libro>> buscarPorIdiomaAutorYTitulo(
            @Param("idioma") String idioma,
            @Param("titulo") String titulo,
            @Param("autor") String autor);
}
