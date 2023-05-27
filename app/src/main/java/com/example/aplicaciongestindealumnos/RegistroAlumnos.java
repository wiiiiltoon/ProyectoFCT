package com.example.aplicaciongestindealumnos;

import android.content.Context;
import android.content.Intent;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;


public class RegistroAlumnos extends AppCompatActivity {
    Context context;
    FirebaseFirestore db;
    FirebaseStorage storage;
    CollectionReference alumnosRefDB;
    StorageReference storageRef;
    ArrayList<Alumno> listaAlumnos;
    ArrayList<String> listaAsignaturas;
    ArrayList<String> listaAsignaturaAlumno;
    ListView listViewAlumnos;
    AdaptadorAlumnos adaptadorAlumnos;
    String emailDB;
    TextView volver;
    public RegistroAlumnos(Context context, ArrayList<Alumno> listaAlumnos, ArrayList<String> listaAsignaturas, String emailDB) {
        this.context = context;

        this.listaAlumnos = listaAlumnos;
        inicializarListas();
        inicializarFireBase();

        relacionXML();
        accionVolver();
        recibirIntent();
        construirListaAlumnos();
        inicializarFireBase();
        accionPulsarAlumno();


    }

    public void volverAtras(View view) {
        onBackPressed();
    }

    private void inicializarListas(){
        listaAsignaturaAlumno = new ArrayList<>();
    }
    private void relacionXML() {
        listViewAlumnos = findViewById(R.id.listaAlumnos);
        volver = findViewById(R.id.textoVolver);
    }

    private void accionVolver() {
        SpannableString spannableString = new SpannableString("Volver");
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        volver.setText(spannableString);
    }

    private void recibirIntent() {
        Intent i = getIntent();
        listaAlumnos = i.getParcelableArrayListExtra("listaAlumnos");
        emailDB = i.getStringExtra("correoUsuario");
        listaAsignaturas = i.getStringArrayListExtra("listaAsignaturas");
    }

    private void construirListaAlumnos() {
        adaptadorAlumnos = new AdaptadorAlumnos(RegistroAlumnos.this, listaAlumnos);
        listViewAlumnos.setAdapter(adaptadorAlumnos);
    }

    private void inicializarFireBase() {
        db = SingletonFirebase.getFireBase();
        storage = SingletonFirebase.getStorage();
        alumnosRefDB = db.collection("users").document(emailDB).collection("alumnos");
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

    private void accionPulsarAlumno() {
        listViewAlumnos.setOnItemClickListener((parent, view, position, id) -> mostrarEliminarAlumno(position));
    }

    private void mostrarEliminarAlumno(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistroAlumnos.this)
                .setTitle("Eliminar alumno")
                .setMessage("¿Estás seguro de que quieres eliminar este alumno?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarAlumno(position))
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void eliminarAlumno(int position) {
        Alumno alumnoSeleccionado = (Alumno) adaptadorAlumnos.getItem(position);
        String idAlumno = alumnoSeleccionado.getIdFireBase();
        DocumentReference alumnoRef = db.collection("users").document(emailDB).collection("alumnos").document(idAlumno);
        alumnoRef.delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getApplicationContext(), "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                    adaptadorAlumnos.remove(alumnoSeleccionado);
                    adaptadorAlumnos.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "No se pudo eliminar el alumno", Toast.LENGTH_SHORT).show());
    }

    public void volver(View view) {
        finish();
    }


}
