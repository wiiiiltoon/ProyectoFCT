package com.example.aplicaciongestindealumnos;

import java.util.ArrayList;

public class GestorAlumnos  {
    private ArrayList<Alumno> listaAlumnos;

    public GestorAlumnos() {
        listaAlumnos = new ArrayList<>();
    }

    public void añadirAlumno(Alumno alumno) {
        listaAlumnos.add(alumno);
    }

    public void eliminarAlumno(Alumno alumno) {
        listaAlumnos.remove(alumno);
    }

    public ArrayList<Alumno> obtenerTodosAlumnos() {
        return listaAlumnos;
    }

    public void setListaAlumnos(ArrayList<Alumno> listaAlumnos) {
        this.listaAlumnos = listaAlumnos;
    }

    public Alumno obtenerAlumno(String id) {
        for (Alumno alumno : listaAlumnos) {
            if (alumno.getIdFireBase().equals(id)) {
                return alumno;
            }
        }
        return null;
    }
}