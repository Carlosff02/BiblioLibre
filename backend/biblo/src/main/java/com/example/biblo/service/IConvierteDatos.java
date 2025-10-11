package com.example.biblo.service;

public interface IConvierteDatos {
    <T> T obtenerDatos(String json, Class<T> clase);
}
