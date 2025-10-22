package com.example.biblo.models;

import com.example.biblo.dto.LibroDTO;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Entity
@Table(name = "libro")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Libro {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idlibro;
    private String titulo;
    @ManyToOne()
    @JoinColumn(name = "autor_id")
    private Autor autor;
    private String idioma;
    private Integer descargas;
    private String imgSrc;
    private String textHtml;
    private String epub;
    @ManyToOne
    @JoinColumn(name = "idpagina")  // <-- nombre real de la FK en la tabla libro
    @JsonIgnore
    private PaginasGuardadas paginaGuardada;



    public Libro(LibroDTO libro){

        this.titulo= libro.titulo();
        this.descargas= libro.descargas();
        this.idioma=libro.idioma().get(0);
        this.imgSrc = libro.formatos().imageJpeg();
        this.textHtml=libro.formatos().textHtml();
        this.epub=libro.formatos().epub();
    }


}
