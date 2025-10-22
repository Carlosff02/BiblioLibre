package com.example.biblo.service;

import com.example.biblo.dto.AutorDTO;
import com.example.biblo.dto.LibroDTO;
import com.example.biblo.models.Autor;
import com.example.biblo.models.Libro;
import com.example.biblo.models.PaginasGuardadas;
import com.example.biblo.repository.AutorRepository;
import com.example.biblo.repository.LibroRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class LibroService {

    private LibroRepository libroRepository;
    private AutorRepository autorRepository;
    private final PaginasGuardadasRepository paginasGuardadasRepository;
    @PersistenceContext
    private EntityManager entityManager;

    public LibroService(LibroRepository libroRepository, AutorRepository autorRepository, PaginasGuardadasRepository paginasGuardadasRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
        this.paginasGuardadasRepository = paginasGuardadasRepository;
    }

    @Transactional
    public Page<Libro> buscarPorIdioma(String idioma, int page) throws IOException, InterruptedException {
        if (idioma.equalsIgnoreCase("español")) {
            idioma = "es";
        } else if (idioma.equalsIgnoreCase("english")) {
            idioma = "en";
        } else {
            return Page.empty();
        }

        int pageSize = 32; // Gutendex usa 32 libros por página

        // 🔍 Verificar si la página ya está en BD
        Optional<PaginasGuardadas> paginaGuardadaBuscar =
                paginasGuardadasRepository.findByIdiomaAndNumeroPagina(idioma, page);

        if (paginaGuardadaBuscar.isPresent()) {
            List<Libro> librosGuardados = paginaGuardadaBuscar.get().getLibros();
            return new PageImpl<>(librosGuardados, PageRequest.of(page - 1, pageSize), paginaGuardadaBuscar.get().getTotalRegistros());
        }

        // 🌐 Llamar API Gutendex
        String urlStr = "https://gutendex.com/books/?languages=" + idioma + "&page=" + page;
        System.out.println(urlStr);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlStr))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String json = response.body();

        if (json.isEmpty()) {
            System.out.println("No se encontraron libros.");
            return Page.empty();
        }

        // 🧩 Crear un mapper JSON robusto
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);

        long totalElements = root.get("count").asLong();
        JsonNode resultsNode = root.get("results");

        if (resultsNode == null || !resultsNode.isArray() || resultsNode.isEmpty()) {
            System.out.println("No se encontraron libros.");
            return Page.empty();
        }

        List<LibroDTO> datos = Arrays.asList(mapper.treeToValue(resultsNode, LibroDTO[].class));
        List<Libro> libros = new ArrayList<>();

        // 🗂 Crear entidad de página
        PaginasGuardadas paginaConsultada = new PaginasGuardadas(
                null, idioma, page, totalElements, LocalDateTime.now(), urlStr, libros
        );

        for (LibroDTO libroDTO : datos) {
            Libro libro = new Libro(libroDTO);
            libro.setPaginaGuardada(paginaConsultada);

            Autor autor = null;

            if (libroDTO.autor() != null && !libroDTO.autor().isEmpty()) {
                AutorDTO autorDTO = libroDTO.autor().get(0);
                autor = new Autor(autorDTO);

                // Formatear nombre
                String nombreOriginal = autor.getNombre();
                String[] partes = nombreOriginal.split(",");
                if (partes.length == 2) {
                    String apellido = partes[0].trim();
                    String nombre = partes[1].trim();
                    autor.setNombre(nombre + " " + apellido);
                }

                // Buscar o crear autor existente
                Autor finalAutor = autor;
                autor = autorRepository.findByNombre(autor.getNombre())
                        .orElseGet(() -> autorRepository.save(finalAutor));
            }

            libro.setAutor(autor);
            libros.add(libro);
        }

        // 💾 Guardar página y libros
        paginasGuardadasRepository.save(paginaConsultada);
        libroRepository.saveAll(libros);

        System.out.println("Libros guardados: " + libros.size());

        // 📦 Retornar pageable
        return new PageImpl<>(libros, PageRequest.of(page - 1, pageSize), totalElements);
    }




    @Transactional
    public Libro buscarLibro(String titulo) throws IOException, InterruptedException {
        Optional<Libro> libroBuscar1 = libroRepository.findFirstByTituloContainingIgnoreCase(titulo);

        if(libroBuscar1.isPresent()) {
            return libroBuscar1.get();
        }
        String url_str = "https://gutendex.com/books/";

        var lectura = titulo;
        lectura = lectura.replaceAll(" ", "%20");
        url_str= url_str+"?search="+lectura;
        HttpRequest request = (HttpRequest) HttpRequest.newBuilder()
                .uri(URI.create(url_str))
                .build();
        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response = client
                .send(request, HttpResponse.BodyHandlers.ofString());




        String json = response.body();

        System.out.println(json);
        //Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE).create();

        ConvierteDatos conversor = new ConvierteDatos();
        if(json.isEmpty()){
            System.out.println("No se encontraron libros.");
            return null;

        }
        else {
            LibroDTO datos = conversor.obtenerDatos(json, LibroDTO.class);
            if(datos!=null) {
                Libro libro = new Libro(datos);
                System.out.println(libro);
                Autor autor = new Autor(datos.autor().get(0));
                String nombreOriginal = autor.getNombre(); // Ej: "Cervantes Saavedra, Miguel de"
                String[] partes = nombreOriginal.split(",");
                if (partes.length == 2) {
                    String apellido = partes[0].trim();  // "Cervantes Saavedra"
                    String nombre = partes[1].trim();    // "Miguel de"
                    String nombreFormateado = nombre + " " + apellido; // "Miguel de Cervantes Saavedra"
                    autor.setNombre(nombreFormateado);
                }
                Optional<Autor> autorBuscar = autorRepository.findByNombre(autor.getNombre());


                if(autorBuscar.isEmpty()){




                    autor = autorRepository.save(autor);
                    System.out.println(autor);
                    libro.setAutor(autor);
                } else{



                    libro.setAutor(autorBuscar.get());
                }
                Optional<Libro> libroBuscar = libroRepository.findByTitulo(libro.getTitulo());

                if(libroBuscar.isEmpty()) {
                    System.out.println(libro);
                    libroRepository.save(libro);
                }


                return libro;
            } else{
                System.out.println("No se encontraron libros");
                return  null;
            }
        }

    }

    public List<Libro> buscarLibrosMasPopulares(){
        return libroRepository.buscarLibrosPopulares();


    }

}
