package com.example.aplicaciongestindealumnos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class datosAlumno extends AppCompatActivity {

    ArrayList<String> asignaturasAlumno;
    RecyclerView recycler;
    String nombre, curso;
    TextView campoNombre, campoCurso;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_datos_alumno);

        Intent i = getIntent();
        nombre = i.getStringExtra("nombreAlumno");
        curso = i.getStringExtra("cursoAlumno");

        recycler = findViewById(R.id.listaAlumnos);
        campoNombre = findViewById(R.id.datoNombre);
        campoCurso = findViewById(R.id.datoCurso);

        campoNombre.setText("Nombre: " + nombre);
        campoCurso.setText("Curso: " + curso);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recycler.setLayoutManager(layoutManager);

        asignaturasAlumno = new ArrayList<>();
        asignaturasAlumno.add("Mates");
        asignaturasAlumno.add("Lengua");
        asignaturasAlumno.add("Ingles");
        asignaturasAlumno.add("Historia");

        adaptadorAsignaturas adaptador = new adaptadorAsignaturas(asignaturasAlumno);
        recycler.setAdapter(adaptador);
    }
}