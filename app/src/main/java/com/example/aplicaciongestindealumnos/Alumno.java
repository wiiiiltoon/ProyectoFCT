package com.example.aplicaciongestindealumnos;

public class Alumno {

    private int foto;
    private String nombre;
    private String curso;

    public Alumno(int foto,String nombre, String curso){
        this.foto = foto;
        this.nombre = nombre;
        this.curso = curso;
    }

    public int getFoto() {
        return foto;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCurso() {
        return curso;
    }
}
