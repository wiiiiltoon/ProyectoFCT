package com.example.aplicaciongestindealumnos;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class ConsultarAsignaturas extends AppCompatActivity {
    FirebaseFirestore db;
    ListView listView;
    ArrayList<String> listaAsignaturas;
    ArrayAdapter<String> adaptador;
    TextView volver;
    String emailDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_asignaturas);

        relacionXML();
        accionTextoVolver();
        recibirIntent();
        inicializarFirebase();
        accionVolver();
        construirListViewAlumnos();
    }

    private void relacionXML() {
        listView = findViewById(R.id.listaAsignaturas);
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
        listaAsignaturas = i.getStringArrayListExtra("listaAsignaturas");
    }

    private void inicializarFirebase() {
        db = SingletonFirebase.getFireBase();
    }

    private void accionVolver() {
        SpannableString spannableString = new SpannableString("Volver");
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        volver.setText(spannableString);
    }

    private void construirListViewAlumnos() {
        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaAsignaturas);
        listView.setAdapter(adaptador);
        listView.setOnItemClickListener((parent, view, position, id) -> mostrarDialogoEliminarAsignatura(position));
    }

    private void mostrarDialogoEliminarAsignatura(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(ConsultarAsignaturas.this);
        builder.setTitle("Eliminar asignatura");
        builder.setMessage("¿Está seguro que desea eliminar esta asignatura?");
        builder.setPositiveButton("Eliminar", (dialog, which) -> eliminarAsignatura(position));
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void eliminarAsignatura(int position) {
        String nombreAsignatura = adaptador.getItem(position);
        DocumentReference asignaturaRef = db.collection("users").document(emailDB).collection("asignaturas").document(nombreAsignatura);
        asignaturaRef.delete()
                .addOnSuccessListener(aVoid -> {
                    adaptador.remove(nombreAsignatura);
                    adaptador.notifyDataSetChanged();
                    mostrarMensaje("Asignatura " + nombreAsignatura + " eliminada correctamente");
                })
                .addOnFailureListener(e -> mostrarMensaje("No se pudo eliminar la asignatura"));
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
    }
}