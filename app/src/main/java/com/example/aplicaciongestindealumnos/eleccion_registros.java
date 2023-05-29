package com.example.aplicaciongestindealumnos;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;

public class eleccion_registros extends AppCompatActivity {
    FirebaseFirestore db;
    StorageReference storageRef;
    ArrayList<Alumno> listaAlumnos;
    ArrayList<String> listaAsignaturas;
    String emailDB;
    TextView volver;
    CollectionReference alumnosRefDB, asignaturasRefDB;
    private RegistroAlumnos registroAlumnos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eleccion_registros);

        relacionXML();
        accionTextoVolver();
        recibirIntent();
        inicializarFirebase();
        alumnosRefDB = db.collection("users").document(emailDB).collection("alumnos");
        asignaturasRefDB = db.collection("users").document(emailDB).collection("asignaturas");
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
        onBackPressed();
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
        Log.e("TAG", listaAlumnos.toString());
    }
    public void registrarAsignaturas(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Añadir asignatura");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layoutView = inflater.inflate(R.layout.item_asignatura, null);
        builder.setView(layoutView);

        EditText editTextNombre = layoutView.findViewById(R.id.editTextNombre);

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            String nombreAsignatura = editTextNombre.getText().toString().trim();
            if (!nombreAsignatura.isEmpty()) {
                verificarExistenciaAsignatura(nombreAsignatura);
            } else {
                mostrarMensaje("Ingrese un nombre de asignatura válido");
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void verificarExistenciaAsignatura(String nombreAsignatura) {
        asignaturasRefDB.document(nombreAsignatura).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        mostrarMensaje("La asignatura ya existe");
                    } else {
                        crearNuevaAsignatura(nombreAsignatura);
                    }
                })
                .addOnFailureListener(e -> mostrarMensaje("Error al verificar la existencia de la asignatura"));
    }

    private void crearNuevaAsignatura(String nombreAsignatura) {
        asignaturasRefDB.document(nombreAsignatura)
                .set(new HashMap<>(), SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    listaAsignaturas.add(nombreAsignatura);
                    mostrarMensaje("Asignatura agregada: " + nombreAsignatura);
                })
                .addOnFailureListener(e -> mostrarMensaje("Error al agregar asignatura a Firestore"));
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


    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        registroAlumnos.onActivityResult(requestCode, resultCode, data);
    }
}