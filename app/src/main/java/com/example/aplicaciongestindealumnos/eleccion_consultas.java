package com.example.aplicaciongestindealumnos;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class eleccion_consultas extends AppCompatActivity {

    Button botonAsiganturas, botonAlumnos;
    ArrayList<Alumno> listaAlumnos;
    ArrayList<String> listaAsignaturas;
    String emailDB;
    TextView volver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eleccion_consultas);

        relacionXML();
        accionTextoVolver();
        recibirIntent();
    }
    private void relacionXML(){
        botonAlumnos = findViewById(R.id.botonAlumnos);
        botonAsiganturas = findViewById(R.id.botonAsignaturas);
        volver = findViewById(R.id.textoVolver);
    }
    private void accionTextoVolver() {
        SpannableString spannableString = new SpannableString("Volver");
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        volver.setText(spannableString);
    }
    public void volverAtras(View view) {
        onBackPressed();
    }
    private void recibirIntent(){
        Intent i = getIntent();
        emailDB = i.getStringExtra("correoUsuario");
        listaAlumnos = i.getParcelableArrayListExtra("listaAlumnos");
        listaAsignaturas = i.getStringArrayListExtra("listaAsignaturas");
    }

    public void intentConsultarAlumnos(View view) {
        Intent i = new Intent(eleccion_consultas.this, ConsultarAlumnos.class);
        i.putExtra("correoUsuario", emailDB);
        i.putExtra("listaAlumnos", listaAlumnos);
        startActivity(i);
    }

    public void intentConsultarAsignaturas(View view) {
        Intent i = new Intent(eleccion_consultas.this, ConsultarAsignaturas.class);
        i.putExtra("correoUsuario", emailDB);
        i.putExtra("listaAsignaturas", listaAsignaturas);
        startActivity(i);
    }
}