package com.example.biblo.controller;

import com.example.biblo.dto.LibroDTO;
import com.example.biblo.models.Libro;
import com.example.biblo.service.LibroService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/libros")
@RequiredArgsConstructor
@CrossOrigin("http://localhost:4200")
public class LibroController {

    private final LibroService libroService;

    @GetMapping("/{nombreLibro}")
    public Libro buscarPorNombre(@PathVariable String nombreLibro) throws IOException, InterruptedException {
        return libroService.buscarLibro(nombreLibro);
    }

    @GetMapping("/populares")
    public List<Libro> buscarLibrosPopulares(){
        return libroService.buscarLibrosMasPopulares();
    }

    @GetMapping("/buscar-por-idioma/{idioma}")
    public List<Libro> buscarLibrosPorIdioma(@PathVariable String idioma) throws IOException, InterruptedException {
        return libroService.buscarPorIdioma(idioma);
    }

}
