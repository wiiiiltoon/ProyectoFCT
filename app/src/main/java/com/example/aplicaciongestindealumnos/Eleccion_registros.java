package com.example.aplicaciongestindealumnos;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class Eleccion_registros extends AppCompatActivity {
    FirebaseFirestore db;
    StorageReference storageRef;
    ArrayList<Alumno> listaAlumnos;
    ArrayList<String> listaAsignaturas;
    String emailDB;
    TextView volver;
    CollectionReference alumnosCollection, asignaturasCollection;
    private RegistroAlumnos registroAlumnos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.eleccion_registros);

        relacionXML();
        accionTextoVolver();
        recibirIntent();
        inicializarFirebase();
        alumnosCollection = db.collection("users").document(emailDB).collection("alumnos");
        asignaturasCollection = db.collection("users").document(emailDB).collection("asignaturas");
        registroAlumnos = new RegistroAlumnos(this,listaAlumnos,listaAsignaturas,emailDB);
    }

    private void relacionXML() {
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
    private void recibirIntent() {
        Intent i = getIntent();
        emailDB = i.getStringExtra("correoUsuario");
        listaAlumnos = i.getParcelableArrayListExtra("listaAlumnos");
        listaAsignaturas = i.getStringArrayListExtra("listaAsignaturas");
    }

    private void inicializarFirebase() {
        db = SingletonFirebase.getFireBase();
        storageRef = SingletonFirebase.getReferenciaFotos();
        storageRef.getMetadata().addOnSuccessListener(storageMetadata -> {
        }).addOnFailureListener(exception -> {
            if (exception instanceof StorageException && ((StorageException) exception).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                storageRef.putBytes(new byte[]{0}).addOnSuccessListener(taskSnapshot -> {
                }).addOnFailureListener(exception1 -> {
                });
            }
        });
    }

    public void registrarAlumnos(View view) {
        listaAlumnos =  registroAlumnos.mostrarDialog();
    }


    public void registroCalificacion(View view) {
        if(listaAlumnos.size()==0){
            mostrarMensaje("No hay alumnos registrados");
        }else{
            new RegistroCalificacion(this, listaAlumnos, emailDB);
        }
    }

    public void registroAusencia(View view) {
        if(listaAlumnos.size()==0){
            mostrarMensaje("No hay alumnos registrados");
        }else{
            new RegistroAusencia(this, listaAlumnos, emailDB);
        }
    }

    public void registroAsignatura(View view) {
        new RegistroAsignatura(asignaturasCollection,this,listaAsignaturas);
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        registroAlumnos.onActivityResult(requestCode, resultCode, data);
    }
    public void onBackPressed() {
        volverAtras(null);
    }
}