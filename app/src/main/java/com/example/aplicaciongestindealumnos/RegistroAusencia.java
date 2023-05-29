package com.example.aplicaciongestindealumnos;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class RegistroAusencia {
    private FirebaseFirestore db;
    private CollectionReference asignaturasCollection;
    private CollectionReference alumnosCollection;
    private Context context;
    private List<Alumno> listaAlumnos;
    private ArrayAdapter<String> alumnoAdapter;
    private ArrayAdapter<String> asignaturaAdapter;
    private Spinner spinnerAlumnos;
    private Spinner spinnerAsignaturas;
    private TextView numAusencias;
    private Button restarAusencia, sumarAusencia;
    private String emailDB;

    public RegistroAusencia(Context context, ArrayList<Alumno> listaAlumnos, String emailDB) {
        this.context = context;
        this.emailDB = emailDB;
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
        View view = inflater.inflate(R.layout.pantalla_registro_ausencia, null);

        spinnerAlumnos = view.findViewById(R.id.spinner_alumnos);
        spinnerAsignaturas = view.findViewById(R.id.spinner_asignaturas);
        numAusencias = view.findViewById(R.id.numAusencias);
        restarAusencia = view.findViewById(R.id.restarAusencia);
        sumarAusencia = view.findViewById(R.id.sumarAusencia);

        restarAusencia.setOnClickListener(this::restarAusencia);
        sumarAusencia.setOnClickListener(this::sumarAusencia);

        construirDialog(view);
    }

    public void construirDialog(View view) {
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
                    cargarNumAusencia();
                } else {
                    asignaturaAdapter.clear();
                    numAusencias.setText("");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                asignaturaAdapter.clear();
            }
        });
        spinnerAsignaturas.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                cargarNumAusencia();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                asignaturaAdapter.clear();
            }
        });
        builder.setView(view);
        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            int posicionAlumno = spinnerAlumnos.getSelectedItemPosition();
            if (posicionAlumno > 0) {
                int numAusenciasNuevo = Integer.parseInt(numAusencias.getText().toString());
                ingresarNuevaAusencia(numAusenciasNuevo);
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
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        builder.create().show();
    }

    private void cargarNumAusencia() {
        int posicionAlumno = spinnerAlumnos.getSelectedItemPosition();
        if (posicionAlumno > 0) { // Seleccionó un alumno válido
            Alumno alumno = listaAlumnos.get(posicionAlumno - 1);
            String idFireBaseAlumno = alumno.getIdFireBase();
            String asignatura = (String) spinnerAsignaturas.getSelectedItem();

            asignaturasCollection = alumnosCollection.document(idFireBaseAlumno).collection("asignaturas");
            asignaturasCollection.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentAsignatura : task.getResult()) {
                        if (documentAsignatura.getString("nombre").equals(asignatura)) {
                            Long faltas = documentAsignatura.getLong("faltas");
                            if (faltas != null) {
                                numAusencias.setText(String.valueOf(faltas));
                            } else {
                                mostrarMensaje("Error al cargar las faltas");
                            }
                        }
                    }
                } else {
                    mostrarMensaje("Error al cargar las calificaciones: " + task.getException().getMessage());
                }
            }).addOnFailureListener(e -> {
                mostrarMensaje("Error al cargar las calificaciones: " + e.getMessage());
            });
        }
    }

    private void cargarAsignaturasAlumno(Alumno alumno) {
        List<String> asignaturas = alumno.getAsignaturas();
        asignaturaAdapter.clear();
        asignaturaAdapter.addAll(asignaturas);
        asignaturaAdapter.notifyDataSetChanged();
    }

    private void ingresarNuevaAusencia(int numAusencia) {
        int posicionAlumno = spinnerAlumnos.getSelectedItemPosition();
        if (posicionAlumno > 0) { // Seleccionó un alumno válido
            Alumno alumno = listaAlumnos.get(posicionAlumno - 1);
            String idFireBaseAlumno = alumno.getIdFireBase();
            String asignatura = (String) spinnerAsignaturas.getSelectedItem();

            asignaturasCollection = alumnosCollection.document(idFireBaseAlumno).collection("asignaturas");
            asignaturasCollection.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot documentAsignatura : task.getResult()) {
                        if (documentAsignatura.getString("nombre").equals(asignatura)) {
                            String asignaturaId = documentAsignatura.getId();
                            DocumentReference asignaturaRef = asignaturasCollection.document(asignaturaId);

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("faltas", numAusencia);

                            asignaturaRef.update(updates)
                                    .addOnSuccessListener(aVoid -> mostrarMensaje("Faltas de " + alumno + " en " + asignatura + " actualizadas"))
                                    .addOnFailureListener(e -> mostrarMensaje("No se pudo actualizar el número de faltas"));
                        }
                    }
                } else {
                    mostrarMensaje("Error al cargar las calificaciones: " + task.getException().getMessage());
                }
            }).addOnFailureListener(e -> {
                mostrarMensaje("Error al cargar las calificaciones: " + e.getMessage());
            });
        }
    }

    private void restarAusencia(View view) {
        String numAusenciasStr = numAusencias.getText().toString();
        if (!numAusenciasStr.isEmpty()) {
            int ausencias = Integer.parseInt(numAusenciasStr);
            ausencias--;
            numAusencias.setText(String.valueOf(ausencias));
        } else {
            Toast.makeText(context, "El campo de ausencias está vacío", Toast.LENGTH_SHORT).show();
        }
    }

    private void sumarAusencia(View view) {
        String numAusenciasStr = numAusencias.getText().toString();
        if (!numAusenciasStr.isEmpty()) {
            int ausencias = Integer.parseInt(numAusenciasStr);
            ausencias++;
            numAusencias.setText(String.valueOf(ausencias));
        } else {
            Toast.makeText(context, "El campo de ausencias está vacío", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
    }
}