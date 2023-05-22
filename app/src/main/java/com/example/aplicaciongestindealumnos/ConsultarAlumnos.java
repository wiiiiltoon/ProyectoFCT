package com.example.aplicaciongestindealumnos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;


public class ConsultarAlumnos extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private CollectionReference alumnosRefDB;
    private Uri uriElegidaCliente;
    private StorageReference storageRef;
    private ArrayList<Alumno> listaAlumnos;
    private ArrayList<String> listaAsignaturas;
    private ListView listViewAlumnos;
    private AdaptadorAlumnos adaptadorAlumnos;
    private ImageView imagenSeleccionada;
    private String emailDB;
    private TextView volver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_alumnos);

        relacionXML();
        accionTextoVolver();
        recibirIntent();
        inicializarFirebase();
        construirListViewAlumnos();
        crearReferenciaStorage();
    }

    private void relacionXML() {
        listViewAlumnos = findViewById(R.id.listaAlumnos);
        volver = findViewById(R.id.textoVolver);
    }
    private void accionTextoVolver() {
        SpannableString spannableString = new SpannableString("Volver");
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        volver.setText(spannableString);
    }
    private void recibirIntent() {
        Intent i = getIntent();
        emailDB = i.getStringExtra("correoUsuario");
        listaAlumnos = i.getParcelableArrayListExtra("listaAlumnos");
        listaAsignaturas = i.getStringArrayListExtra("listaAsignaturas");
    }
    private void inicializarFirebase() {
        db = SingletonFirebase.getFireBase();
        storage = SingletonFirebase.getStorage();
        storageRef = SingletonFirebase.getReferenciaFotos();
        alumnosRefDB = db.collection("users").document(emailDB).collection("alumnos");
    }
    private void construirListViewAlumnos() {
        adaptadorAlumnos = new AdaptadorAlumnos(ConsultarAlumnos.this, listaAlumnos);
        listViewAlumnos.setAdapter(adaptadorAlumnos);
        listViewAlumnos.setOnItemClickListener((parent, view, position, id) -> {
            Alumno alumnoSeleccionado = listaAlumnos.get(position);
            String nombre = alumnoSeleccionado.getNombre();
            String curso = alumnoSeleccionado.getCurso();
            String idAlumno = alumnoSeleccionado.getIdFireBase();
            Uri foto = alumnoSeleccionado.getUrlFoto();

            abrirPerfilAlumno(nombre,curso,idAlumno,foto);
        });
    }

    private void abrirPerfilAlumno(String nombre,String curso, String id, Uri foto){
        Intent i = new Intent(ConsultarAlumnos.this, PerfilAlumno.class);
        i.putStringArrayListExtra("listaAsignaturas", listaAsignaturas);
        i.putExtra("nombre", nombre);
        i.putExtra("curso", curso);
        i.putExtra("id", id);
        i.putExtra("foto", foto.toString());
        startActivity(i);
        Toast.makeText(getApplicationContext(), foto.toString(), Toast.LENGTH_SHORT).show();
    }
    private void crearReferenciaStorage() {
        storageRef.getMetadata().addOnSuccessListener(storageMetadata -> {
        }).addOnFailureListener(exception -> {
            if (exception instanceof StorageException && ((StorageException) exception).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                storageRef.putBytes(new byte[]{0}).addOnSuccessListener(taskSnapshot -> {
                }).addOnFailureListener(exception1 -> {
                });
            }
        });
    }

    public void volverAtras(View view) {
        onBackPressed();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            uriElegidaCliente = data.getData();
            imagenSeleccionada.setImageURI(uriElegidaCliente);
        }
    }
}