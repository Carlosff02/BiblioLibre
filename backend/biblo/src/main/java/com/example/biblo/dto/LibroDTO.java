package com.example.biblo.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record LibroDTO(
        @JsonAlias("id") Long id,
        @JsonAlias("title") String titulo,
        @JsonAlias("languages") List<String> idioma,
        @JsonAlias("download_count") Integer descargas,
        @JsonAlias("summaries") List<String> summaries,
        @JsonAlias("subjects") List<String> categorias,
        @JsonAlias("bookshelves") List<String> estantes,
        @JsonAlias("authors") List<AutorDTO> autor,
        @JsonAlias("formats") Formats formatos
                       ) {
}
