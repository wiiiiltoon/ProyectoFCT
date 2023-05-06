package com.example.aplicaciongestindealumnos;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;

import java.util.ArrayList;

public class registroAlumnos extends AppCompatActivity {

    ArrayList<Alumno> alumnos;
    ListView listaAlumnos;
    Adaptador adaptador;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_alumnos);

        alumnos = new ArrayList<>();

        listaAlumnos = findViewById(R.id.listaAlumnos);

        // En caso de hacer clic en un elemento de la lista
        listaAlumnos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Muestra un cuadro de diálogo preguntando si se desea eliminar el alumno
                AlertDialog.Builder builder = new AlertDialog.Builder(registroAlumnos.this);
                builder.setTitle("Eliminar alumno");
                builder.setMessage("¿Está seguro que desea eliminar este alumno?");

                builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Obtiene el elemento seleccionado y lo elimina del array y del adaptador
                        Alumno alumnoSeleccionado = (Alumno) adaptador.getItem(position);
                        adaptador.remove(alumnoSeleccionado);
                        adaptador.notifyDataSetChanged();
                    }
                });

                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }

    public void anadirAlumno(View view){
        AlertDialog.Builder builder = new AlertDialog.Builder(registroAlumnos.this);
        builder.setTitle("Añadir alumno");
        builder.setMessage("Indique los datos del alumno:");

        // Creamos los campos de texto
        EditText campoNombre = new EditText(registroAlumnos.this);
        campoNombre.setHint("Nombre y apellidos");

        EditText campoAñoCurso = new EditText(registroAlumnos.this);
        campoAñoCurso.setHint("Año del curso");

        String[] nivelesEducativos = {"ESO", "BACHILLER", "GRADO MEDIO", "GRADO SUPERIOR", "FP MEDIO", "FP SUPERIOR"};
        Spinner spinnerNivelEducativo = new Spinner(registroAlumnos.this);
        ArrayAdapter<String> adapterNivelEducativo = new ArrayAdapter<>(registroAlumnos.this, android.R.layout.simple_spinner_dropdown_item, nivelesEducativos);

        // Establece el adaptador para el spinner
        spinnerNivelEducativo.setAdapter(adapterNivelEducativo);

        EditText campoAsignatura = new EditText(registroAlumnos.this);
        campoAsignatura.setHint("Asignatura");

        // Creamos un layout para los campos de texto
        LinearLayout layoutCampos = new LinearLayout(registroAlumnos.this);
        layoutCampos.setOrientation(LinearLayout.VERTICAL);

        // Agregamos los campos de texto al layout
        layoutCampos.addView(campoNombre);
        layoutCampos.addView(campoAñoCurso);
        layoutCampos.addView(spinnerNivelEducativo);
        layoutCampos.addView(campoAsignatura);

        // Agregamos el layout al diálogo
        builder.setView(layoutCampos);

        // agregamos botones al cuadro de dialogo y creamos una accion
        builder.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String nombre = campoNombre.getText().toString().trim();
                String anio = campoAñoCurso.getText().toString().trim();
                String nivelEducativo = spinnerNivelEducativo.getSelectedItem().toString();

                String curso = anio+"º "+nivelEducativo;

                // Crear una instancia de Alumno y agregarlo a la lista
                alumnos.add(new Alumno(R.drawable.human, nombre, curso));

                // Crear una instancia del adaptador con la lista de alumnos y establecerlo en la lista
                adaptador = new Adaptador(registroAlumnos.this, alumnos);
                listaAlumnos.setAdapter(adaptador);

            }
        });
        builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }
}