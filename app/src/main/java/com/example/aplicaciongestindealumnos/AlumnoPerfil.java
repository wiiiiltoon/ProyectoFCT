package com.example.aplicaciongestindealumnos;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;

public class AlumnoPerfil extends AppCompatActivity {
    FirebaseStorage storage;
    TextView campoNombre, campoCurso;
    String nombre, curso, idFirebase;
    GridLayout gridAsignaturas;
    ArrayList<String> listaAsignaturas;
    ImageView foto;
    Uri uriFoto;
    String fotografia, emailDB;
    TextView volver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alumno_perfil);

        storage = SingletonFirebase.getStorage();
        listaAsignaturas = new ArrayList<>();

        relacionXML();
        recibirIntent();
        accionTextoVolver();
        cargarDatosAlumno();
        generarAsignaturas();
    }

    private void relacionXML() {
        campoNombre = findViewById(R.id.datoNombre);
        campoCurso = findViewById(R.id.datoCurso);
        volver = findViewById(R.id.textoVolver);
        foto = findViewById(R.id.imagenAlumnos);
        gridAsignaturas = findViewById(R.id.gridAsignaturas);
    }

    private void recibirIntent() {
        Intent i = getIntent();
        listaAsignaturas = i.getStringArrayListExtra("listaAsignaturas");
        emailDB = i.getStringExtra("emailDB");
        nombre = i.getStringExtra("nombre");
        curso = i.getStringExtra("curso");
        idFirebase = i.getStringExtra("idFirebase");
        fotografia = i.getStringExtra("foto");
    }

    private void accionTextoVolver() {
        SpannableString spannableString = new SpannableString("Volver");
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        volver.setText(spannableString);
    }

    private void cargarDatosAlumno() {
        campoNombre.setText(nombre);
        campoCurso.setText(curso);
        Glide.with(this)
                .load(fotografia)
                .into(foto);
    }

    private void generarAsignaturas() {
        GridLayout gridLayout = findViewById(R.id.gridAsignaturas);
        for (String nombre : listaAsignaturas) {
            Button button = new Button(this);
            button.setText(nombre);
            button.setTextColor(Color.WHITE);
            button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            button.setBackgroundResource(R.drawable.botones);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.setGravity(Gravity.CENTER);
            params.width = 350;
            button.setLayoutParams(params);
            button.setOnClickListener(v -> {
                Intent intent = new Intent(AlumnoPerfil.this, AlumnoAsignatura.class);
                intent.putExtra("idFirebase", idFirebase);
                intent.putExtra("nombreAsignatura", nombre);
                intent.putExtra("emailDB", emailDB);
                startActivity(intent);
            });

            gridLayout.addView(button);
        }
    }


    public void volverAtras(View view) {
        onBackPressed();
    }
}