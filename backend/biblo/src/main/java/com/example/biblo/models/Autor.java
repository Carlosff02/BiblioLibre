package com.example.biblo.models;

import com.example.biblo.dto.AutorDTO;
import jakarta.persistence.*;

@Entity
@Table(name="autor")
public class Autor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nombre;
    private Integer fechanacimiento;
    private Integer fechafallecimiento;

    public Autor(){}

    public Autor(Long id, String nombre, Integer fechanacimiento, Integer fechafallecimiento) {
        this.id = id;
        this.nombre = nombre;
        this.fechanacimiento = fechanacimiento;
        this.fechafallecimiento = fechafallecimiento;
    }

    public Autor(AutorDTO autor){

        this.nombre= autor.nombre();
        this.fechanacimiento=autor.fechaNacimiento();
        this.fechafallecimiento= autor.fechaFallecimiento();

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Integer getFechanacimiento() {
        return fechanacimiento;
    }

    public void setFechanacimiento(Integer fechanacimiento) {
        this.fechanacimiento = fechanacimiento;
    }

    public Integer getFechafallecimiento() {
        return fechafallecimiento;
    }

    public void setFechafallecimiento(Integer fechafallecimiento) {
        this.fechafallecimiento = fechafallecimiento;
    }

    @Override
    public String toString() {
        return "Autor{" +
                "id=" + id +
                ", nombre='" + nombre + '\'' +
                ", fechanacimiento='" + fechanacimiento + '\'' +
                ", fechafallecimiento='" + fechafallecimiento + '\'' +
                '}';
    }
}
