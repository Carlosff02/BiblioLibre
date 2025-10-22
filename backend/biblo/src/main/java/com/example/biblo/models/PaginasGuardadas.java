package com.example.biblo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class PaginasGuardadas {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String idioma;
    private Integer numeroPagina;
    private Long totalRegistros;
    private LocalDateTime fechaConsulta;
    private String fuente;
    @OneToMany(mappedBy = "paginaGuardada", cascade = CascadeType.PERSIST)
    private List<Libro> libros;
}
