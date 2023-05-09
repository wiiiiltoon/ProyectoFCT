package com.example.aplicaciongestindealumnos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Space;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class registroAlumnos extends AppCompatActivity {
    FirebaseFirestore db;
    ArrayList<Alumno> alumnos;
    ListView listaAlumnos;
    Adaptador adaptador;
    ImageView imagenSeleccionada;
    String emailDB;
    DocumentReference referenciaDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_alumnos);

        db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        //Obtenemos el email
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            emailDB = currentUser.getEmail();
            //hacemos referencia a la collecion
            referenciaDB = db.collection("users").document(emailDB);
        } else {
            Log.w("principal", "El usuario actual es nulo");
        }
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

    public void anadirAlumno(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(registroAlumnos.this);
        builder.setTitle("Añadir alumno");
        builder.setMessage("Indique los datos del alumno:");

        // Boton para añadir imagen
        Button botonImagen = new Button(registroAlumnos.this);
        botonImagen.setText("Seleccionar imagen");

        // Campo de nombre y apellidos
        EditText campoNombre = new EditText(registroAlumnos.this);
        campoNombre.setHint("Nombre y apellidos");

        // Campo para añdir el año del curso
        EditText campoAñoCurso = new EditText(registroAlumnos.this);
        campoAñoCurso.setHint("Año del curso");

        // Campo desplegable de Nivel Educativo
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
        layoutCampos.setGravity(Gravity.CENTER);
        layoutCampos.setPadding(50, 50, 50, 50);

        // Agregamos los componentes al layout
        imagenSeleccionada = new ImageView(registroAlumnos.this);
        imagenSeleccionada.setImageResource(R.drawable.logo);
        imagenSeleccionada.setLayoutParams(new LinearLayout.LayoutParams(500, 500));
        imagenSeleccionada.setAdjustViewBounds(true);

        layoutCampos.addView(imagenSeleccionada);
        layoutCampos.addView(botonImagen);
        layoutCampos.addView(campoNombre);
        layoutCampos.addView(campoAñoCurso);
        layoutCampos.addView(spinnerNivelEducativo);
        layoutCampos.addView(campoAsignatura);

        // Agregamos el layout al diálogo
        builder.setView(layoutCampos);

        botonImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 1);
            }
        });

        // agregamos botones al cuadro de dialogo y creamos una accion
        builder.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String nombre = campoNombre.getText().toString().trim();
                String anio = campoAñoCurso.getText().toString().trim();
                String nivelEducativo = spinnerNivelEducativo.getSelectedItem().toString();
                String curso = anio + "º " + nivelEducativo;

                // Crear una instancia de Alumno y agregarlo a la lista
                alumnos.add(new Alumno(imagenSeleccionada, nombre, curso));

                // Crear una instancia del adaptador con la lista de alumnos y establecerlo en la lista
                adaptador = new Adaptador(registroAlumnos.this, alumnos);
                listaAlumnos.setAdapter(adaptador);

                /*//Agregamos a firebase
                Map<String, Object> nuevoAlumnoMap = new HashMap<>();
                nuevoAlumnoMap.put("nombre", nombre);
                nuevoAlumnoMap.put("curso", curso);

                // Añadir el nuevo alumno a la colección
                referenciaDB.collection("alumnos").add(nuevoAlumnoMap)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Toast.makeText(getApplicationContext(), "Alumno agregado con exito con ID: ", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(getApplicationContext(), "Error al agregar alumno a Firestore", Toast.LENGTH_SHORT).show();

                            }
                        });*/
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri selectedImage = data.getData();
            imagenSeleccionada.setImageURI(selectedImage);
        }
    }
}
