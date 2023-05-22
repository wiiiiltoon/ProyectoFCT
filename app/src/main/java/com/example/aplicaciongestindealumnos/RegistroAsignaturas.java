package com.example.aplicaciongestindealumnos;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class RegistroAsignaturas extends AppCompatActivity {
    FirebaseFirestore db;
    CollectionReference alumnosRefDB;
    Button botonCrear;
    EditText campoAsignatura;
    ListView listView;
    ArrayList<String> listaAsignaturas;
    ArrayAdapter<String> adaptador;
    TextView volver;
    String emailDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_asignaturas);

        relacionXML();
        recibirIntent();
        inicializarFirebase();
        accionVolver();
        construirListViewAlumnos();
    }

    private void relacionXML() {
        botonCrear = findViewById(R.id.botonCrear);
        campoAsignatura = findViewById(R.id.campoAsignatura);
        listView = findViewById(R.id.listaAsignaturas);
        volver = findViewById(R.id.textoVolver);
    }

    public void volverAtras(View view) {
        onBackPressed();
    }
    private void recibirIntent() {
        listaAsignaturas = new ArrayList<>();
        Intent i = getIntent();
        emailDB = i.getStringExtra("correoUsuario");
        listaAsignaturas = i.getStringArrayListExtra("listaAsignaturas");
    }
    private void inicializarFirebase() {
        db = SingletonFirebase.getFireBase();
        alumnosRefDB = db.collection("users").document(emailDB).collection("asignaturas");
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
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistroAsignaturas.this);
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
                    mostrarMensaje("Eliminado correctamente");
                })
                .addOnFailureListener(e -> mostrarMensaje("No se pudo eliminar la asignatura"));
    }
    public void crearAsignatura(View view) {
        String nombreAsignatura = campoAsignatura.getText().toString().replaceAll("\\s+", " ").trim().trim();
        if (nombreAsignatura.isEmpty()) {
            mostrarMensaje("Ingrese un nombre para la asignatura");
        } else {
            verificarExistenciaAsignatura(nombreAsignatura);
        }
    }
    private void verificarExistenciaAsignatura(String nombreAsignatura) {
        alumnosRefDB.document(nombreAsignatura).get()
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
        alumnosRefDB.document(nombreAsignatura)
                .set(new HashMap<>(), SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    campoAsignatura.setText(null);
                    mostrarMensaje("Asignatura creada correctamente");
                    listaAsignaturas.add(nombreAsignatura);
                    adaptador.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> mostrarMensaje("Error al crear la asignatura"));
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
    }
}