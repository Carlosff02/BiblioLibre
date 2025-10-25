package com.example.biblo.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

@RestController
@RequestMapping("/api/visor-epub")
@CrossOrigin("*") // ✅ Solo este CORS es necesario
public class EpubProxyController {

    private final RestTemplate restTemplate = new RestTemplate();
    @Value("${epub.cache.dir:epubs-cache}")
    private String epubCacheDir;

    @GetMapping("/epub")
    public ResponseEntity<byte[]> proxyEpub(@RequestParam String url) {
        try {
            // 📁 Crear carpeta si no existe (usa el valor configurable)
            Path epubDir = Paths.get(epubCacheDir);
            if (!Files.exists(epubDir)) {
                Files.createDirectories(epubDir);
            }

            // 🧩 Crear nombre de archivo seguro basado en URL
            String fileName = url.replaceAll("[^a-zA-Z0-9.-]", "_") + ".epub";
            Path epubPath = epubDir.resolve(fileName);

            byte[] epubBytes;

            if (Files.exists(epubPath)) {
                // ⚡ Ya existe en caché → leer desde disco
                epubBytes = Files.readAllBytes(epubPath);
                System.out.println("📚 Cargando EPUB desde caché: " + epubPath);
            } else {
                // 🌐 No existe → descargar desde internet
                System.out.println("⬇️ Descargando EPUB desde: " + url);
                epubBytes = restTemplate.getForObject(url, byte[].class);

                if (epubBytes == null || epubBytes.length == 0) {
                    throw new IOException("No se pudo descargar el archivo desde la URL.");
                }

                // 💾 Guardar en disco
                Files.write(epubPath, epubBytes, StandardOpenOption.CREATE);
                System.out.println("💾 EPUB guardado en caché: " + epubPath);
            }

            // 📤 Enviar EPUB al frontend
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("Content-Disposition", "inline; filename=\"" + fileName + "\"");

            return new ResponseEntity<>(epubBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(("Error al descargar o leer el EPUB: " + e.getMessage()).getBytes());
        }
    }

}
