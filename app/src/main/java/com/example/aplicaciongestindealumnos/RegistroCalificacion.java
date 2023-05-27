package com.example.aplicaciongestindealumnos;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RegistroCalificacion {
    FirebaseFirestore db;
    CollectionReference asignaturasCollection;
    private Context context;
    private List<Alumno> listaAlumnos;
    private ArrayAdapter<String> alumnoAdapter;
    private ArrayAdapter<String> asignaturaAdapter;
    Spinner spinnerAlumnos;
    Spinner spinnerAsignaturas;
    EditText editTextDescripcion;
    EditText editTextNota;


    public RegistroCalificacion(Context context, ArrayList<Alumno> listaAlumnos) {
        this.context = context;
        this.listaAlumnos = listaAlumnos;
        inicializarFirebase();
        mostrarDialog();
    }
    private void inicializarFirebase(){
        db = SingletonFirebase.getFireBase();

    }
    private void mostrarDialog(){
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.activity_registro_calificacion, null);

        spinnerAlumnos = view.findViewById(R.id.spinner_alumnos);
        spinnerAsignaturas = view.findViewById(R.id.spinner_asignaturas);
        editTextDescripcion = view.findViewById(R.id.edit_text_descripcion);
        editTextNota = view.findViewById(R.id.edit_text_nota);

        construirDialog(view);
    }
    public void construirDialog(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Agregar Calificación");

        // Crear una lista de nombres de alumnos
        List<String> nombresAlumnos = new ArrayList<>();
        for (Alumno alumno : listaAlumnos) {
            nombresAlumnos.add(alumno.getNombre());
        }

        alumnoAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, nombresAlumnos);
        spinnerAlumnos.setAdapter(alumnoAdapter);

        asignaturaAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, new ArrayList<>());
        spinnerAsignaturas.setAdapter(asignaturaAdapter);

        // Configurar listener para el spinner de alumnos
        spinnerAlumnos.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Alumno alumnoSeleccionado = listaAlumnos.get(position);
                cargarAsignaturasAlumno(alumnoSeleccionado);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // No se seleccionó ningún alumno
                asignaturaAdapter.clear();
            }
        });

        builder.setView(view);


        // Configurar botones del diálogo
        builder.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Obtener los valores seleccionados y el texto ingresado
                Alumno alumno = (Alumno) spinnerAlumnos.getSelectedItem();
                String asignatura = (String) spinnerAsignaturas.getSelectedItem();
                String descripcion = editTextDescripcion.getText().toString();
                int nota = Integer.parseInt(editTextNota.getText().toString());

                // Crear la calificación con los datos ingresados
                //Calificacion calificacion = new Calificacion(alumno, asignatura, descripcion, nota);
                Calificacion calificacion = new Calificacion(new Date(), "Examen 1", 1);

                // Realizar las operaciones necesarias con la calificación

                // Mostrar mensaje de éxito
                Toast.makeText(context, "Calificación añadida", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Mostrar el diálogo
        builder.create().show();
    }

    private void cargarAsignaturasAlumno(Alumno alumno) {
        List<String> asignaturas = alumno.getAsignaturas();

        asignaturaAdapter.clear();
        asignaturaAdapter.addAll(asignaturas);
        asignaturaAdapter.notifyDataSetChanged();
    }
}