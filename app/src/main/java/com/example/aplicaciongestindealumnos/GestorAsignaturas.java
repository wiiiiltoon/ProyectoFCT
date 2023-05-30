package com.example.aplicaciongestindealumnos;

import java.util.ArrayList;

public class GestorAsignaturas {
    private ArrayList<String> listaAsignaturas;

    public GestorAsignaturas() {
        listaAsignaturas = new ArrayList<>();
    }

    public void añadirAsignatura(String asignatura) {
        listaAsignaturas.add(asignatura);
    }

    public void eliminarAsignatura(String asignatura) {
        listaAsignaturas.remove(asignatura);
    }

    public ArrayList<String> obtenerTodasAsignaturas() {
        return listaAsignaturas;
    }

    public void setListaAsignaturas(ArrayList<String> listaAsignaturas) {
        this.listaAsignaturas = listaAsignaturas;
    }
}