package com.example.aplicaciongestindealumnos;

import android.widget.ImageView;

public class Alumno {

    private ImageView foto;
    private String nombre;
    private String curso;

    public Alumno(ImageView foto,String nombre, String curso){
        this.foto = foto;
        this.nombre = nombre;
        this.curso = curso;
    }

    public ImageView getFoto() {
        return foto;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCurso() {
        return curso;
    }
}
