package com.example.biblo.service;

import com.example.biblo.dto.LibroDTO;
import com.example.biblo.models.Autor;
import com.example.biblo.models.Libro;
import com.example.biblo.repository.AutorRepository;
import com.example.biblo.repository.LibroRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class LibroService {

    private LibroRepository libroRepository;
    private AutorRepository autorRepository;

    public LibroService(LibroRepository libroRepository, AutorRepository autorRepository) {
        this.libroRepository = libroRepository;
        this.autorRepository = autorRepository;
    }

    public List<Libro> buscarPorIdioma(String idioma) throws IOException, InterruptedException {
        if(idioma.equalsIgnoreCase("español")){
            idioma = "es";
        } else if(idioma.equalsIgnoreCase("english")){
            idioma = "en";
        } else{
            return  null;
        }
        String url_str = "https://gutendex.com/books/";
        url_str= url_str+"?languages="+idioma;

        HttpRequest request = (HttpRequest) HttpRequest.newBuilder()
                .uri(URI.create(url_str))
                .build();
        HttpClient client = HttpClient.newHttpClient();

        HttpResponse<String> response = client
                .send(request, HttpResponse.BodyHandlers.ofString());

        String json = response.body();

        ConvierteDatos conversor = new ConvierteDatos();
        if(json.isEmpty()){
            System.out.println("No se encontraron libros.");
            return null;

        }

        else {
            List<LibroDTO> datos = conversor.obtenerDatos(json, List.class);
            if(!datos.isEmpty()) {


            List<Libro> libros = new ArrayList<>();
            for(LibroDTO libroDTO : datos){
                Libro libro = new Libro(libroDTO);
                libros.add(libro);
            }

                return libros;
            } else{
                System.out.println("No se encontraron libros");
                return  null;
            }
        }

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
