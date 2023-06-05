package com.example.aplicaciongestindealumnos;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Iterator;

public class AlumnoPerfil extends AppCompatActivity {
    FirebaseFirestore db;
    FirebaseStorage storage;
    TextView campoNombre, campoCurso;
    String nombre, curso, idFirebase;
    GridLayout gridAsignaturas;
    ArrayList<String> listaAsignaturas;
    ArrayList<Alumno> listaAlumnos;
    ImageView foto;
    String fotografia, emailDB;
    TextView volver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alumno_perfil);

        storage = SingletonFirebase.getStorage();
        listaAsignaturas = new ArrayList<>();
        db = SingletonFirebase.getFireBase();

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
        listaAlumnos = i.getParcelableArrayListExtra("listaAlumnos");
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
            params.width = 380;
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
    public void mostrarEliminarAlumno(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle("Eliminar alumno")
                .setMessage("¿Estás seguro de que quieres eliminar este alumno?")
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarAlumno(idFirebase))
                .setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void eliminarAlumno(String id) {
        DocumentReference alumnoRef = db.collection("users").document(emailDB).collection("alumnos").document(id);
        alumnoRef.delete()
                .addOnSuccessListener(aVoid -> {
                    boolean eliminado = false;
                    Iterator<Alumno> iterator = listaAlumnos.iterator();
                    while (iterator.hasNext()) {
                        Alumno alumno = iterator.next();
                        if (alumno.getIdFireBase().equals(id)) {
                            iterator.remove();
                            eliminado = true;
                        }
                    }
                    if (eliminado) {
                        mostrarMensaje("Alumnos eliminados correctamente");
                    } else {
                        mostrarMensaje("No se encontraron alumnos con ese ID");
                    }
                    volverAtras(null);
                })
                .addOnFailureListener(e -> mostrarMensaje("No se pudo eliminar el alumno"));
    }

    private void mostrarMensaje(String mensaje){
        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
    }

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