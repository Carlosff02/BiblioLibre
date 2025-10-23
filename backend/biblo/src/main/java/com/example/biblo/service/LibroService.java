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
import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.swing.text.html.Option;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

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

        // 📘 Si el libro existe
        if (libroBuscar1.isPresent()) {
            Libro libroExistente = libroBuscar1.get();

            // 🔍 Actualizar descripción y categorías si están vacías
            if ((libroExistente.getDescripcion() == null || libroExistente.getDescripcion().isBlank()) ||
                    (libroExistente.getCategorias() == null || libroExistente.getCategorias().isEmpty())) {

                System.out.println("🔍 Libro con datos incompletos. Buscando información en Gutendex...");

                // 🧠 Buscar datos del libro por título en Gutendex
                Libro datosGutendex = obtenerLibroDesdeGutendex(libroExistente.getIdgutendex());

                if (datosGutendex != null) {

                    // 📄 Si falta descripción, actualizarla
                    if (libroExistente.getDescripcion() == null || libroExistente.getDescripcion().isBlank()) {
                        if (datosGutendex.getDescripcion() != null && !datosGutendex.getDescripcion().isBlank()) {
                            String descripcionTraducida = traducirADescripcionEspanol(datosGutendex.getDescripcion(), "auto");
                            libroExistente.setDescripcion(descripcionTraducida);
                            System.out.println("✅ Descripción actualizada para: " + libroExistente.getTitulo());
                        } else {
                            System.out.println("⚠️ No se encontró descripción en Gutendex.");
                        }
                    }

                    // 🏷️ Si no tiene categorías, actualizarlas
                    if (libroExistente.getCategorias() == null || libroExistente.getCategorias().isEmpty()) {
                        if (datosGutendex.getCategorias() != null && !datosGutendex.getCategorias().isEmpty()) {
                            List<String> categoriasTraducidas = libroExistente.getCategorias().stream()
                                    .filter(Objects::nonNull)
                                    .map(cat -> traducirADescripcionEspanol(cat, "auto"))
                                    .collect(Collectors.toList());
                            libroExistente.setCategorias(categoriasTraducidas);
                            System.out.println("✅ Categorías actualizadas para: " + libroExistente.getTitulo());
                        } else {
                            System.out.println("⚠️ No se encontraron categorías en Gutendex.");
                        }
                    }

                    // 💾 Guardar cambios
                    libroRepository.save(libroExistente);
                } else {
                    System.out.println("⚠️ No se encontró información en Gutendex para: " + titulo);
                }
            }

            return libroExistente;
        }

        // 📚 Si no existe, buscar en Gutendex
        String urlStr = "https://gutendex.com/books/?search=" + URLEncoder.encode(titulo, StandardCharsets.UTF_8);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(urlStr))
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String json = response.body();

        if (json == null || json.isEmpty()) {
            System.out.println("⚠️ No se encontraron libros en Gutendex.");
            return null;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        JsonNode resultsNode = root.path("results");

        if (!resultsNode.isArray() || resultsNode.isEmpty()) {
            System.out.println("⚠️ No se encontraron resultados en Gutendex.");
            return null;
        }

        // 🧩 Tomar primer libro
        JsonNode primerLibro = resultsNode.get(0);
        LibroDTO datos = mapper.treeToValue(primerLibro, LibroDTO.class);
        Libro libro = new Libro(datos);

        // 🧠 Traducir descripción si existe
        if (datos.summaries() != null && !datos.summaries().isEmpty()) {
            String descripcionTraducida = traducirADescripcionEspanol(datos.summaries().get(0), datos.idioma().isEmpty() ? "auto" : datos.idioma().get(0));
            libro.setDescripcion(descripcionTraducida);
        }

        // 👤 Procesar autor
        if (datos.autor() != null && !datos.autor().isEmpty()) {
            AutorDTO autorDTO = datos.autor().get(0);
            Autor autor = new Autor(autorDTO);

            String nombreOriginal = autor.getNombre();
            String[] partes = nombreOriginal.split(",");
            if (partes.length == 2) {
                autor.setNombre(partes[1].trim() + " " + partes[0].trim());
            }

            Autor autorEntity = autorRepository.findByNombre(autor.getNombre())
                    .orElseGet(() -> autorRepository.save(autor));

            libro.setAutor(autorEntity);
        }

        // 🏷️ Traducir categorías
        if (datos.categorias() != null && !datos.categorias().isEmpty()) {
            List<String> categoriasTraducidas = datos.categorias().stream()
                    .filter(Objects::nonNull)
                    .map(cat -> traducirADescripcionEspanol(cat, "auto"))
                    .collect(Collectors.toList());
            libro.setCategorias(categoriasTraducidas);
        }



        // 💾 Guardar libro
        libroRepository.save(libro);
        System.out.println("💾 Libro guardado: " + libro.getTitulo());

        return libro;
    }

    private Libro obtenerLibroDesdeGutendex(Long id) {
        try {
            String url = "https://gutendex.com/books/" + id;
            System.out.println("🔗 Consultando Gutendex: " + url);
            RestTemplate restTemplate = new RestTemplate();
            String jsonResponse = restTemplate.getForObject(url, String.class);

            JsonNode root = new ObjectMapper().readTree(jsonResponse);

            // ✅ "root" ya contiene el objeto del libro directamente
            Libro libro = new Libro();

            // Gutendex usa "summaries" en lugar de "description"
            if (root.has("summaries") && root.path("summaries").isArray() && root.path("summaries").size() > 0) {
                libro.setDescripcion(root.path("summaries").get(0).asText());
            } else {
                libro.setDescripcion(null);
            }

            // Extraer categorías
            List<String> categorias = new ArrayList<>();
            for (JsonNode subjectNode : root.path("subjects")) {
                categorias.add(subjectNode.asText());
            }
            libro.setCategorias(categorias);

            return libro;

        } catch (Exception e) {
            System.err.println("⚠️ Error obteniendo libro desde Gutendex: " + e.getMessage());
        }
        return null;
    }



    private String obtenerDescripcionDesdeGutendex(String titulo) {
        try {
            String url = "https://gutendex.com/books/?search=" + URLEncoder.encode(titulo, StandardCharsets.UTF_8);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String json = response.body();

            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(json);
            JsonNode results = root.path("results");

            if (results.isArray() && results.size() > 0) {
                JsonNode primerLibro = results.get(0);
                JsonNode summaries = primerLibro.path("summaries");
                if (summaries.isArray() && summaries.size() > 0) {
                    return summaries.get(0).asText();
                }
            }
            return null;
        } catch (Exception e) {
            System.out.println("⚠️ Error al obtener descripción desde Gutendex: " + e.getMessage());
            return null;
        }
    }


    private String traducirADescripcionEspanol(String textoOriginal, String idiomaOrigen) {
        if (textoOriginal == null || textoOriginal.isBlank()) return textoOriginal;

            idiomaOrigen = detectarIdioma(textoOriginal);


        String idiomaNormalizado = idiomaOrigen.toLowerCase().trim();

        // 🔹 Si ya es español, no traducir
        if (idiomaNormalizado.equals("es") || idiomaNormalizado.equals("spa")) {
            System.out.println("✅ Texto ya en español, no se traduce");
            return textoOriginal;
        }

        try {
            // 🔹 Llamar a LibreTranslate local (sin fragmentar, soporta textos más largos)
            return traducirConLibreTranslate(textoOriginal, idiomaNormalizado, "es");

        } catch (Exception e) {
            System.out.println("⚠️ Error al traducir: " + e.getMessage());
            return textoOriginal; // Fallback: devolver original
        }
    }

    private String traducirConLibreTranslate(String texto, String idiomaOrigen, String idiomaDestino)
            throws Exception {

        ObjectMapper mapper = new ObjectMapper();

        // 🔹 Crear JSON request
        Map<String, String> payload = new HashMap<>();
        payload.put("q", texto);
        payload.put("source", idiomaOrigen);
        payload.put("target", idiomaDestino);
        payload.put("format", "text");

        String jsonPayload = mapper.writeValueAsString(payload);

        // 🔹 Llamar a LibreTranslate LOCAL (puerto 5000)
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:5000/translate"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .timeout(Duration.ofSeconds(30))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new Exception("Error HTTP " + response.statusCode() + ": " + response.body());
        }

        JsonNode root = mapper.readTree(response.body());
        String traduccion = root.path("translatedText").asText();

        if (traduccion == null || traduccion.isBlank()) {
            throw new Exception("Traducción vacía recibida");
        }

        System.out.println("✅ Traducido: " + texto.substring(0, Math.min(50, texto.length()))
                + "... → " + traduccion.substring(0, Math.min(50, traduccion.length())) + "...");

        return traduccion;
    }

    private static final LanguageDetector detector = LanguageDetectorBuilder
            .fromAllLanguages()
            .build();

    private String detectarIdioma(String texto) {
        try {
            Language idioma = detector.detectLanguageOf(texto);
            String codigo = idioma.getIsoCode639_1().toString().toLowerCase();
            System.out.println("🔍 Idioma detectado (local): " + codigo);
            return codigo;
        } catch (Exception e) {
            System.out.println("⚠️ Error al detectar idioma: " + e.getMessage());
            return "en";
        }
    }







    public List<Libro> buscarLibrosMasPopulares(){
        return libroRepository.buscarLibrosPopulares();


    }

    @Transactional
    public Page<Libro> buscarLibros(String titulo, String autor, String idioma, int page)
            throws IOException, InterruptedException {

        String tituloQ = titulo != null ? titulo.trim().toLowerCase() : "";
        String autorQ = autor != null ? autor.trim().toLowerCase() : "";
        String idiomaQ = idioma != null ? idioma.trim().toLowerCase() : "";

        System.out.println("🔎 Buscando libros => Título: " + tituloQ + ", Autor: " + autorQ + ", Idioma: " + idiomaQ);

        int pageSize = 32;
        String baseUrl = "https://gutendex.com/books/";
        StringBuilder urlBuilder = new StringBuilder(baseUrl);
        urlBuilder.append("?page=").append(page);

        // 🧩 Normalizar idioma
        if (!idiomaQ.isEmpty() && !idiomaQ.equals("todos")) {
            switch (idiomaQ.toLowerCase()) {
                case "español", "es", "castellano" -> idiomaQ = "es";
                case "english", "ingles", "en" -> idiomaQ = "en";
                case "french", "francés", "fr" -> idiomaQ = "fr";
                case "deutsch", "aleman", "de" -> idiomaQ = "de";
                case "italian", "italiano", "it" -> idiomaQ = "it";
                case "portuguese", "portugues", "pt" -> idiomaQ = "pt";
                case "dutch", "neerlandes", "holandes", "nl" -> idiomaQ = "nl";
                case "danish", "danes", "da" -> idiomaQ = "da";
                case "swedish", "sueco", "sv" -> idiomaQ = "sv";
                case "norwegian", "noruego", "no" -> idiomaQ = "no";
                case "finnish", "finlandes", "fi" -> idiomaQ = "fi";
                case "greek", "griego", "el" -> idiomaQ = "el";
                case "latin", "la" -> idiomaQ = "la";
                case "polish", "polaco", "pl" -> idiomaQ = "pl";
                case "russian", "ruso", "ru" -> idiomaQ = "ru";
                case "chinese", "chino", "zh" -> idiomaQ = "zh";
                case "japanese", "japones", "ja" -> idiomaQ = "ja";
                case "arabic", "arabe", "ar" -> idiomaQ = "ar";
                case "hungarian", "hungaro", "hu" -> idiomaQ = "hu";
                case "czech", "checo", "cs" -> idiomaQ = "cs";
            }
            urlBuilder.append("&languages=").append(idiomaQ);
        }

        // 1️⃣ Buscar solo por idioma
        if (StringUtils.hasText(idioma) && !StringUtils.hasText(autor) && !StringUtils.hasText(titulo)) {
            Optional<PaginasGuardadas> paginaGuardadaBuscar =
                    paginasGuardadasRepository.findByIdiomaAndNumeroPaginaConLibrosYAutores(idiomaQ, page);

            if (paginaGuardadaBuscar.isPresent()) {
                System.out.println("✅ Página encontrada en BD (" + idiomaQ + " pág. " + page + ")");
                List<Libro> librosGuardados = paginaGuardadaBuscar.get().getLibros();

                return new PageImpl<>(
                        librosGuardados,
                        PageRequest.of(page - 1, pageSize),
                        paginaGuardadaBuscar.get().getTotalRegistros()
                );
            }
        }

// 2️⃣ Idioma + Autor
        else if (StringUtils.hasText(idioma) && StringUtils.hasText(autor) && !StringUtils.hasText(titulo)) {
            Optional<List<Libro>> librosPorAutor = libroRepository.buscarPorIdiomaYAutor(idiomaQ, autorQ);
            return construirPagina(librosPorAutor, page, pageSize);
        }

// 3️⃣ Idioma + Título
        else if (StringUtils.hasText(idioma) && !StringUtils.hasText(autor) && StringUtils.hasText(titulo)) {
            Optional<List<Libro>> librosPorTitulo = libroRepository.buscarPorIdiomaYTitulo(idiomaQ, tituloQ);
            return construirPagina(librosPorTitulo, page, pageSize);
        }

// 4️⃣ Idioma + Autor + Título
        else if (StringUtils.hasText(idioma) && StringUtils.hasText(autor) && StringUtils.hasText(titulo)) {
            Optional<List<Libro>> librosPorAmbos =
                    libroRepository.buscarPorIdiomaAutorYTitulo(idiomaQ, tituloQ, autorQ);
            return construirPagina(librosPorAmbos, page, pageSize);
        }


        // 📚 Construir búsqueda textual
        StringBuilder searchQuery = new StringBuilder();
        if (!tituloQ.isEmpty()) searchQuery.append(tituloQ);
        if (!autorQ.isEmpty()) {
            if (searchQuery.length() > 0) searchQuery.append(" ");
            searchQuery.append(autorQ);
        }
        if (searchQuery.length() > 0) {
            urlBuilder.append("&search=")
                    .append(URLEncoder.encode(searchQuery.toString(), StandardCharsets.UTF_8));
        }

        String url = urlBuilder.toString();
        System.out.println("🌍 URL generada: " + url);

        // 🌐 Llamada a Gutendex
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String json = response.body();

        if (json == null || json.isEmpty()) {
            System.out.println("⚠️ Respuesta vacía de Gutendex.");
            return Page.empty();
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(json);
        long totalElements = root.path("count").asLong(0);
        JsonNode resultsNode = root.path("results");

        if (resultsNode == null || !resultsNode.isArray() || resultsNode.isEmpty()) {
            System.out.println("⚠️ No se encontraron resultados.");
            return Page.empty();
        }

        List<LibroDTO> datos = Arrays.asList(mapper.treeToValue(resultsNode, LibroDTO[].class));
        List<Libro> libros = new ArrayList<>();

        // 🗂 Crear entidad PaginasGuardadas
        PaginasGuardadas paginaConsultada = new PaginasGuardadas(
                null, idiomaQ, page, totalElements, LocalDateTime.now(), url, libros
        );

        for (LibroDTO libroDTO : datos) {
            Libro libro = new Libro(libroDTO);
            libro.setPaginaGuardada(paginaConsultada);

            // 👤 Autor
            if (libroDTO.autor() != null && !libroDTO.autor().isEmpty()) {
                AutorDTO autorDTO = libroDTO.autor().get(0);
                Autor autorEntity = new Autor(autorDTO);

                String nombreOriginal = autorEntity.getNombre();
                String[] partes = nombreOriginal.split(",");
                if (partes.length == 2) {
                    autorEntity.setNombre(partes[1].trim() + " " + partes[0].trim());
                }

                Autor finalAutor = autorEntity;
                autorEntity = autorRepository.findByNombre(autorEntity.getNombre())
                        .orElseGet(() -> autorRepository.save(finalAutor));

                libro.setAutor(autorEntity);
            }

            // 🏷️ Categorías traducidas (usando idioma del libro)
//            if (libroDTO.categorias() != null && !libroDTO.categorias().isEmpty()) {
//                String finalIdiomaQ = idiomaQ;
//                List<String> categoriasTraducidas = libroDTO.categorias().stream()
//                        .filter(Objects::nonNull)
//                        .map(cat -> traducirADescripcionEspanol(cat, finalIdiomaQ))
//                        .collect(Collectors.toList()); // ✅ Lista mutable
//                libro.setCategorias(categoriasTraducidas);
//                System.out.println(categoriasTraducidas);
//            }
//
//            if(libroDTO.summaries()!=null && !libroDTO.summaries().isEmpty()){
//                String descripcionTraducida = traducirADescripcionEspanol(libroDTO.summaries().get(0), idiomaQ);
//                libro.setDescripcion(descripcionTraducida);
//            }
            libro.setDescripcion("");
            libro.setCategorias(new ArrayList<>());
            libros.add(libro);
        }

        // 💾 Guardar resultados
        paginasGuardadasRepository.save(paginaConsultada);
        libroRepository.saveAll(libros);



        System.out.println("💾 Página guardada en BD: " + idiomaQ + " pág. " + page + " (" + libros.size() + " libros)");

        return new PageImpl<>(libros, PageRequest.of(page - 1, pageSize), totalElements);
    }





    /**
     * 🧩 Método auxiliar para crear PageImpl con paginación segura
     */
    private Page<Libro> construirPagina(Optional<List<Libro>> librosOpt, int page, int pageSize) {
        if (librosOpt.isEmpty() || librosOpt.get().isEmpty()) {
            return Page.empty();
        }

        List<Libro> libros = librosOpt.get();
        int total = libros.size();

        int fromIndex = Math.min((page - 1) * pageSize, total);
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<Libro> pageContent = libros.subList(fromIndex, toIndex);
        return new PageImpl<>(pageContent, PageRequest.of(page - 1, pageSize), total);
    }


}
