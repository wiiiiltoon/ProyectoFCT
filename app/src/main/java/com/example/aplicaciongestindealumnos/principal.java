package com.example.aplicaciongestindealumnos;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class principal extends AppCompatActivity {

    private FirebaseAuth mAuth;
    TextView datoNombre, datoCentro;
    Button consulta, registro, calificaciones, ausencias, calendario, informe, cerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //Obtenemos el email
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String emailDB = currentUser.getEmail();
            datoNombre = findViewById(R.id.datoNombre);
            datoCentro = findViewById(R.id.datoCentro);
            consulta = findViewById(R.id.botonConsulta);
            registro = findViewById(R.id.botonRegistro);
            calificaciones = findViewById(R.id.botonCalificaciones);
            ausencias = findViewById(R.id.botonAusencias);
            calendario = findViewById(R.id.botonCalendario);
            informe = findViewById(R.id.botonInforme);
            cerrarSesion = findViewById(R.id.cerrarSesion);

            //hacemos referencia a la collecion
            DocumentReference docRef = db.collection("users").document(emailDB);
            docRef.get().addOnSuccessListener(documentSnapshot -> {
                //si existe el datos con el email
                if (documentSnapshot.exists()) {
                    String nombreDB = documentSnapshot.getString("nombre");
                    String centroDB = documentSnapshot.getString("centro");
                    String infoNombre = getString(R.string.nombre_mas_string, nombreDB);
                    datoNombre.setText(infoNombre);
                    String infoCentro = getString(R.string.centro_mas_string, centroDB);
                    datoCentro.setText(infoCentro);
                }
            });
        } else {
            Log.w("principal", "El usuario actual es nulo");
        }
    }

    public void cerrarSesion(View view) {
        mostrarCerrarSesion();
    }
    public void registroAlumnos(View view) {
        Intent i = new Intent(principal.this,registroAlumnos.class);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        mostrarCerrarSesion();
    }

    private void mostrarCerrarSesion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cerrar sesión");
        builder.setMessage("¿Estás seguro de que quieres cerrar sesión?");
        builder.setPositiveButton("Sí", (dialog, which) -> {
            mAuth.signOut();
            Intent i = new Intent(principal.this, inicioSesion.class);
            startActivity(i);
            finish();
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }
}
