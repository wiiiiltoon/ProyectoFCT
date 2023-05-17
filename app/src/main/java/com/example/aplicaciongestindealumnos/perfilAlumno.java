package com.example.aplicaciongestindealumnos;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class perfilAlumno extends AppCompatActivity {
    FirebaseStorage storage;
    StorageReference storageRef;
    String  nombre, curso,idFirebase, asignaturas;
    ImageView foto;
    Uri uriFoto;
    String fotografia;
    Button botonDatoAlumno, botonAusencias, botonCalificaciones;
    RecyclerView recycler;
    TextView volver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_perfil_alumno);

        storage = SingletonFirebase.getStorage();

        Intent i = getIntent();
        nombre = i.getStringExtra("nombreAlumno");
        curso = i.getStringExtra("cursoAlumno");
        idFirebase = i.getStringExtra("idAlumno");
        fotografia = i.getStringExtra("fotoAlumno");
        volver = findViewById(R.id.textoVolver);
        SpannableString spannableString = new SpannableString("Volver");
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        volver.setText(spannableString);
        foto = findViewById(R.id.imagenAlumnos);
        botonDatoAlumno = findViewById(R.id.botonDatosAlumno);
        botonAusencias = findViewById(R.id.botonAusencias);
        botonCalificaciones = findViewById(R.id.botonCalificaciones);

        Glide.with(this)
                .load(fotografia)
                .into(foto);

    }

    public void datosAlumno(View view) {
        TextView datoNombre;
        TextView datoCurso;
        ArrayList<String> asignaturasAlumno;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_datos_alumno, null);
        builder.setView(dialogView);

        datoNombre = dialogView.findViewById(R.id.datoNombre);
        datoCurso = dialogView.findViewById(R.id.datoCurso);
        recycler = dialogView.findViewById(R.id.listaAlumnos);

        datoNombre.setText("Nombre: " + nombre);
        datoCurso.setText("Curso: " + curso);

        GridLayoutManager layoutManager = new GridLayoutManager(this, 3);
        recycler.setLayoutManager(layoutManager);

        asignaturasAlumno = new ArrayList<>();
        asignaturasAlumno.add("Mates");
        asignaturasAlumno.add("Lengua");
        asignaturasAlumno.add("Ingles");
        asignaturasAlumno.add("Historia");

        adaptadorAsignaturas adaptador = new adaptadorAsignaturas(asignaturasAlumno);
        recycler.setAdapter(adaptador);

        builder.setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public void volverAtras(View view) {
        // Acción de volver atrás
        onBackPressed();
    }
}