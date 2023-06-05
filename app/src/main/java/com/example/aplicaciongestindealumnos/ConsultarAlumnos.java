package com.example.aplicaciongestindealumnos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;


public class ConsultarAlumnos extends AppCompatActivity {
    private StorageReference storageRef;
    private ArrayList<Alumno> listaAlumnos;
    private ListView listViewAlumnos;
    private AdaptadorAlumnos adaptadorAlumnos;
    private String emailDB;
    private TextView volver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.consultas_alumnos);

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
    }

    private void inicializarFirebase() {
        storageRef = SingletonFirebase.getReferenciaFotos();
    }

    private void construirListViewAlumnos() {
        adaptadorAlumnos = new AdaptadorAlumnos(ConsultarAlumnos.this, listaAlumnos);
        listViewAlumnos.setAdapter(adaptadorAlumnos);
        listViewAlumnos.setOnItemClickListener((parent, view, position, id) -> {
            Alumno alumnoSeleccionado = listaAlumnos.get(position);
            String nombre = alumnoSeleccionado.getNombre();
            String curso = alumnoSeleccionado.getCurso();
            String idAlumno = alumnoSeleccionado.getIdFireBase();
            ArrayList asignaturas = alumnoSeleccionado.getAsignaturas();
            Uri foto = alumnoSeleccionado.getUrlFoto();

            abrirPerfilAlumno(nombre, curso, idAlumno, foto, asignaturas);
        });
    }

    private void abrirPerfilAlumno(String nombre, String curso, String id, Uri foto, ArrayList asignaturas) {
        Intent i = new Intent(ConsultarAlumnos.this, AlumnoPerfil.class);
        i.putExtra("emailDB", emailDB);
        i.putExtra("nombre", nombre);
        i.putExtra("curso", curso);
        i.putExtra("idFirebase", id);
        i.putExtra("foto", foto.toString());
        i.putStringArrayListExtra("listaAsignaturas", asignaturas);
        i.putParcelableArrayListExtra("listaAlumnos", listaAlumnos);
        launcherAlumnos.launch(i);
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
    private ActivityResultLauncher<Intent> launcherAlumnos = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    listaAlumnos = data.getParcelableArrayListExtra("listasDevueltas");
                    adaptadorAlumnos.setListaAlumnos(listaAlumnos);
                }
            }
    );

    public void volverAtras(View view){
        Intent i = new Intent();
        i.putParcelableArrayListExtra("listasDevueltas", listaAlumnos);
        setResult(RESULT_OK, i);
        finish();
    }
    public void onBackPressed() {
        volverAtras(null);
    }
}