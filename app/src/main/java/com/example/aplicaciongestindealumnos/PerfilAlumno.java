package com.example.aplicaciongestindealumnos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class PerfilAlumno extends AppCompatActivity {
    FirebaseStorage storage;
    String nombre, curso, idFirebase;
    RecyclerView recyclerAsignatura;
    AdaptadorAsignaturas adaptadorAsignatura;
    ArrayList<String> listaAsignaturas;
    ImageView foto;
    Uri uriFoto;
    String fotografia;
    TextView volver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_alumno);

        storage = SingletonFirebase.getStorage();
        listaAsignaturas = new ArrayList<>();

        relacionXML();
        recibirIntent();
        accionTextoVolver();
        cargarFoto();
        generarAsignaturas();
    }

    private void relacionXML() {
        volver = findViewById(R.id.textoVolver);
        foto = findViewById(R.id.imagenAlumnos);
        recyclerAsignatura = findViewById(R.id.listaAsignaturas);
    }

    private void recibirIntent() {
        Intent i = getIntent();
        listaAsignaturas = i.getStringArrayListExtra("listaAsignaturas");
        nombre = i.getStringExtra("nombre");
        curso = i.getStringExtra("curso");
        idFirebase = i.getStringExtra("idAlumno");
        fotografia = i.getStringExtra("foto");
    }

    private void accionTextoVolver() {
        SpannableString spannableString = new SpannableString("Volver");
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        volver.setText(spannableString);
    }

    private void cargarFoto() {
        Glide.with(this)
                .load(fotografia)
                .into(foto);
    }

    private void generarAsignaturas() {
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(this, 2);
        recyclerAsignatura.setLayoutManager(layoutManager);
        adaptadorAsignatura = new AdaptadorAsignaturas(listaAsignaturas);
        recyclerAsignatura.setAdapter(adaptadorAsignatura);
    }

    public void volverAtras(View view) {
        onBackPressed();
    }
}