package com.example.biblo.service;

import com.example.biblo.dto.LibroDTO;
import com.example.biblo.models.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class ConvierteDatos implements IConvierteDatos{
    private ObjectMapper objectMapper = new ObjectMapper();


    @Override
    public <T> T obtenerDatos(String json, Class<T> clase) {
        try {
            ApiResponse apiResponse = objectMapper.readValue(json, ApiResponse.class);

            if (apiResponse != null && apiResponse.getResults() != null && !apiResponse.getResults().isEmpty()) {

                // Si el tipo pedido es una lista, devuelves toda la lista
                if (clase.equals(List.class)) {
                    return (T) apiResponse.getResults();
                }

                // Si piden solo un objeto, devuelves el primero
                return (T) apiResponse.getResults().get(0);
            }

            return null;
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al deserializar JSON: " + e.getMessage(), e);
        }
    }


}
