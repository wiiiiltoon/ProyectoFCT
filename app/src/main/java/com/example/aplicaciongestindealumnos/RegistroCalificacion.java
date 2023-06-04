package com.example.aplicaciongestindealumnos;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RegistroCalificacion {
    private FirebaseFirestore db;
    private CollectionReference asignaturasCollection;
    private CollectionReference alumnosCollection;
    private Context context;
    private List<Alumno> listaAlumnos;
    private ArrayAdapter<String> alumnoAdapter;
    private ArrayAdapter<String> asignaturaAdapter;
    private Spinner spinnerAlumnos;
    private Spinner spinnerAsignaturas;
    private EditText campoDescripcion;
    private EditText campoNota;

    public RegistroCalificacion(Context context, ArrayList<Alumno> listaAlumnos, String emailDB) {
        this.context = context;
        this.listaAlumnos = listaAlumnos;

        inicializarFirebase();
        mostrarDialog();

        alumnosCollection = db.collection("users").document(emailDB).collection("alumnos");
    }

    private void inicializarFirebase() {
        db = SingletonFirebase.getFireBase();
    }

    private void mostrarDialog() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_registro_calificacion, null);

        spinnerAlumnos = view.findViewById(R.id.spinner_alumnos);
        spinnerAsignaturas = view.findViewById(R.id.spinner_asignaturas);
        campoDescripcion = view.findViewById(R.id.edit_text_descripcion);
        campoNota = view.findViewById(R.id.edit_text_nota);

        registrarCalificacion(view);
    }

    public void registrarCalificacion(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Agregar Calificación");

        List<String> nombresAlumnos = new ArrayList<>();
        nombresAlumnos.add("Seleccione un alumno...");
        for (Alumno alumno : listaAlumnos) {
            nombresAlumnos.add(alumno.getNombre());
        }

        alumnoAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, nombresAlumnos);
        spinnerAlumnos.setAdapter(alumnoAdapter);

        asignaturaAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spinnerAsignaturas.setAdapter(asignaturaAdapter);

        spinnerAlumnos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position > 0) {
                    Alumno alumnoSeleccionado = listaAlumnos.get(position - 1);
                    cargarAsignaturasAlumno(alumnoSeleccionado);
                } else {
                    asignaturaAdapter.clear();
                    campoDescripcion.setText("");
                    campoNota.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                asignaturaAdapter.clear();
            }
        });

        builder.setView(view);

        builder.setPositiveButton("Añadir", (dialog, which) -> {
            int posicionAlumno = spinnerAlumnos.getSelectedItemPosition();
            if (posicionAlumno > 0) {
                agregarCalificacion();
                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                mostrarMensaje("Debe elegir un alumno");
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

    private void agregarCalificacion() {
        int posicionAlumno = spinnerAlumnos.getSelectedItemPosition();
        Alumno alumno = listaAlumnos.get(posicionAlumno-1);
        String asignatura = (String) spinnerAsignaturas.getSelectedItem();
        String descripcion = campoDescripcion.getText().toString();
        String notaText = campoNota.getText().toString();

        if (descripcion.isEmpty()||notaText.isEmpty()) {
            mostrarMensaje("Debe rellenar los campos");
            return;
        }

        Double nota = Double.parseDouble(notaText);
        if(nota<0||nota>10){
            mostrarMensaje("Ingrese una nota valida (0-10)");
            return;
        }

        asignaturasCollection = alumnosCollection.document(alumno.getIdFireBase()).collection("asignaturas");
        asignaturasCollection.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot documentAsignatura : task.getResult()) {
                    if (documentAsignatura.getString("nombre").equals(asignatura)) {
                        String idAsignatura = documentAsignatura.getId();
                        CollectionReference calificacionCollection = asignaturasCollection.document(idAsignatura).collection("calificaciones");

                        Map<String, Object> nuevoDocumento = new HashMap<>();
                        nuevoDocumento.put("fecha", new Timestamp(new Date()));
                        nuevoDocumento.put("nombre", descripcion);
                        nuevoDocumento.put("nota", nota);

                        calificacionCollection.add(nuevoDocumento)
                                .addOnSuccessListener(documentReference -> {
                                    mostrarMensaje("Calificacion agregada para " + alumno.getNombre() + " en " + asignatura);
                                })
                                .addOnFailureListener(e -> {
                                });
                    }
                }
            } else {
                mostrarMensaje("Error al cargar las calificaciones: " + task.getException().getMessage());
            }
        }).addOnFailureListener(e -> {
            mostrarMensaje("Error al cargar las calificaciones: " + e.getMessage());
        });
    }

    private void cargarAsignaturasAlumno(Alumno alumno) {
        List<String> asignaturas = alumno.getAsignaturas();

        asignaturaAdapter.clear();
        asignaturaAdapter.addAll(asignaturas);
        asignaturaAdapter.notifyDataSetChanged();
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
    }
}
