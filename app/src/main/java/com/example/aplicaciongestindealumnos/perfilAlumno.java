package com.example.aplicaciongestindealumnos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class perfilAlumno extends AppCompatActivity {
    FirebaseStorage storage;
    StorageReference storageRef;
    String  nombre, curso,idFirebase, asignaturas;
    ImageView foto;
    Uri uriFoto;
    String fotografia;
    Button botonDatoAlumno, botonAusencias, botonCalificaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_alumno);

        storage = SingletonFirebase.getStorage();

        Intent i = getIntent();
        nombre = i.getStringExtra("nombreAlumno");
        curso = i.getStringExtra("cursoAlumno");
        idFirebase = i.getStringExtra("idAlumno");
        fotografia = i.getStringExtra("fotoAlumno");

        foto = findViewById(R.id.imagenAlumnos);
        botonDatoAlumno = findViewById(R.id.botonDatosAlumno);
        botonAusencias = findViewById(R.id.botonAusencias);
        botonCalificaciones = findViewById(R.id.botonCalificaciones);

        Glide.with(this)
                .load(fotografia)
                .into(foto);

    }
}