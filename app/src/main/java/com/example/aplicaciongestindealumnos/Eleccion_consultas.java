package com.example.aplicaciongestindealumnos;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class Eleccion_consultas extends AppCompatActivity {
    ArrayList<Alumno> listaAlumnos;
    ArrayList<String> listaAsignaturas;
    String emailDB;
    TextView volver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eleccion_consultas);

        relacionXML();
        accionTextoVolver();
        recibirIntent();
    }
    private void relacionXML(){
        volver = findViewById(R.id.textoVolver);
    }
    private void accionTextoVolver() {
        SpannableString spannableString = new SpannableString("Volver");
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        volver.setText(spannableString);
    }
    public void volverAtras(View view) {
        ResultadoListasDevueltas resultado = new ResultadoListasDevueltas(listaAlumnos, listaAsignaturas);
        Intent i = new Intent();
        i.putExtra("listasDevueltas", resultado);
        setResult(RESULT_OK, i);
        finish();
    }
    private void recibirIntent(){
        Intent i = getIntent();
        emailDB = i.getStringExtra("correoUsuario");
        listaAlumnos = i.getParcelableArrayListExtra("listaAlumnos");
        listaAsignaturas = i.getStringArrayListExtra("listaAsignaturas");
    }

    public void intentConsultarAlumnos(View view) {
        Intent i = new Intent(Eleccion_consultas.this, ConsultarAlumnos.class);
        i.putExtra("correoUsuario", emailDB);
        i.putExtra("listaAlumnos", listaAlumnos);
        launcherAlumnos.launch(i);
    }

    public void intentConsultarAsignaturas(View view) {
        Intent i = new Intent(Eleccion_consultas.this, ConsultarAsignaturas.class);
        i.putExtra("correoUsuario", emailDB);
        i.putExtra("listaAsignaturas", listaAsignaturas);
        launcherAsignaturas.launch(i);
    }
    private ActivityResultLauncher<Intent> launcherAsignaturas = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    listaAsignaturas = data.getStringArrayListExtra("listasDevueltas");
                }
            }
    );
    private ActivityResultLauncher<Intent> launcherAlumnos = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    listaAlumnos = data.getParcelableArrayListExtra("listasDevueltas");
                }
            }
    );
    public void onBackPressed() {
        volverAtras(null);
    }
}