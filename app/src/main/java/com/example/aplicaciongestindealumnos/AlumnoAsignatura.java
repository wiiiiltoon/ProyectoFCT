package com.example.aplicaciongestindealumnos;

import android.content.Intent;
import android.os.Bundle;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class AlumnoAsignatura extends AppCompatActivity {

    FirebaseFirestore db;
    CollectionReference asignaturasCollection;
    private GridView gridView;
    private TextView titulo;
    private String emailDB, idFirebase, nombreAsignatura;
    private ArrayList<Calificacion> calificaciones;
    private AdaptadorCalificaciones adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alumno_asignatura);

        relacionXML();
        recogerIntent();
        inicilizarFirebase();

        calificaciones = new ArrayList<>();

        asignaturasCollection = db.collection("users").document(emailDB).collection("alumnos")
                .document(idFirebase).collection("asignaturas");
        adapter = new AdaptadorCalificaciones(this, calificaciones);
        gridView.setAdapter(adapter);
        cargarCalificaciones(nombreAsignatura);


    }

    private void relacionXML() {
        gridView = findViewById(R.id.gridAsignaturas);
        titulo = findViewById(R.id.titulo);
    }

    private void recogerIntent() {
        Intent i = getIntent();
        idFirebase = i.getStringExtra("idFirebase");
        nombreAsignatura = i.getStringExtra("nombreAsignatura");
        titulo.setText(nombreAsignatura);
        emailDB = i.getStringExtra("emailDB");
    }

    private void inicilizarFirebase() {
        db = SingletonFirebase.getFireBase();
    }

    private void cargarCalificaciones(String nombreAsig) {
        asignaturasCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentAsignatura : task.getResult()) {
                    if (documentAsignatura.getString("nombre").equals(nombreAsig)) {
                        obtenerCalificaciones(documentAsignatura.getId());
                    }
                }
            } else {
                mostrarMensaje("Error al cargar las calificaciones: " + task.getException().getMessage());
            }
        }).addOnFailureListener(e -> {
            mostrarMensaje("Error al cargar las calificaciones: " + e.getMessage());
        });
    }

    private void obtenerCalificaciones(String asignaturaId) {
        asignaturasCollection.document(asignaturaId).collection("calificaciones").get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentCalificacion : task.getResult()) {
                    Calificacion calificacion = crearCalificacionDesdeDocumento(documentCalificacion);
                    calificaciones.add(calificacion);
                    adapter.notifyDataSetChanged();
                }
            } else {
                mostrarMensaje("Error al obtener las calificaciones: " + task.getException().getMessage());
            }
        }).addOnFailureListener(e -> {
            mostrarMensaje("Error al obtener las calificaciones: " + e.getMessage());
        });
    }

    private Calificacion crearCalificacionDesdeDocumento(QueryDocumentSnapshot document) {
        Timestamp fecha = document.getTimestamp("fecha");
        String nombre = document.getString("nombre");
        int nota = document.getLong("nota").intValue();
        return new Calificacion(fecha.toDate(), nombre, nota);
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
    }
}