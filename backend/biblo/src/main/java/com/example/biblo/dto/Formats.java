package com.example.biblo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;


@JsonIgnoreProperties(ignoreUnknown = true)
public record Formats(
        @JsonProperty("image/jpeg") String imageJpeg,
        @JsonProperty("text/plain") String textPlain,
        @JsonProperty("text/html") String textHtml,
        @JsonProperty("application/epub+zip") String epub
) {
}
