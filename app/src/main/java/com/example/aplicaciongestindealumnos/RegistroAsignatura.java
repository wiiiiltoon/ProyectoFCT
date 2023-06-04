package com.example.aplicaciongestindealumnos;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.SetOptions;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;

public class RegistroAsignatura {
    CollectionReference asignaturasCollection;
    Context contexto;
    ArrayList<String> listaAsignaturas;

    public RegistroAsignatura(CollectionReference asignaturasCollection, Context contexto, ArrayList<String> listaAsignaturas) {
        this.asignaturasCollection = asignaturasCollection;
        this.contexto = contexto;
        this.listaAsignaturas = listaAsignaturas;
        registrarAsignaturas();
    }

    private void registrarAsignaturas() {
        AlertDialog.Builder builder = new AlertDialog.Builder(contexto);
        builder.setTitle("Añadir asignatura");

        LayoutInflater inflater = LayoutInflater.from(contexto);
        View layoutView = inflater.inflate(R.layout.view_registro_asginatura, null);
        builder.setView(layoutView);

        EditText editTextNombre = layoutView.findViewById(R.id.editTextNombre);

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            String nombreAsignatura = editTextNombre.getText().toString().trim();
            if (!nombreAsignatura.isEmpty()) {
                verificarExistenciaAsignatura(nombreAsignatura);
                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mostrarMensaje("Debe ingresar un nombre de asignatura");
                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            try {
                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                field.setAccessible(true);
                field.set(dialog, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            dialog.dismiss();
        });
        builder.create().show();
    }

    private void verificarExistenciaAsignatura(String nombreAsignatura) {
        asignaturasCollection.document(nombreAsignatura).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        mostrarMensaje("La asignatura ya existe");
                    } else {
                        crearNuevaAsignatura(nombreAsignatura);
                    }
                })
                .addOnFailureListener(e -> mostrarMensaje("Error al verificar la existencia de la asignatura"));
    }

    private void crearNuevaAsignatura(String nombreAsignatura) {
        asignaturasCollection.document(nombreAsignatura)
                .set(new HashMap<>(), SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    listaAsignaturas.add(nombreAsignatura);
                    mostrarMensaje("Asignatura agregada: " + nombreAsignatura);
                })
                .addOnFailureListener(e -> mostrarMensaje("Error al agregar asignatura a Firestore"));
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(contexto, mensaje, Toast.LENGTH_SHORT).show();
    }
}
