package com.example.biblo.models;

import com.example.biblo.dto.LibroDTO;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ApiResponse {
    @JsonAlias("results")
    List<LibroDTO> results;

    // Getters y Setters
    public List<LibroDTO> getResults() {
        return results;
    }

    public void setResults(List<LibroDTO> results) {
        this.results = results;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "results=" + results +
                '}';
    }
}
