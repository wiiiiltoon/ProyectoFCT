package com.example.aplicaciongestindealumnos;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class principal extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    TextView datoNombre;
    TextView datoCentro;
    Button consulta;
    Button registro;
    Button calificaciones;
    Button ausencias;
    Button calendario;
    Button informe;
    Button cerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        //Obtenemos el email
        FirebaseUser currentUser = mAuth.getCurrentUser();
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
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                //si existe el datos con el email
                if (documentSnapshot.exists()) {
                    String nombreDB = documentSnapshot.getString("nombre");
                    String centroDB = documentSnapshot.getString("centro");
                    datoNombre.setText("Nombre: " + nombreDB);
                    datoCentro.setText("Centro: " + centroDB);
                }
            }
        });

    }


    public void cerrarSesion(View view){
        mostrarCerrarSesion();
    }
    @Override
    public void onBackPressed() {
        mostrarCerrarSesion();
    }

    private void mostrarCerrarSesion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cerrar sesión");
        builder.setMessage("¿Estás seguro de que quieres cerrar sesión?");
        builder.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mAuth.signOut();
                Intent i = new Intent(principal.this, inicioSesion.class);
                startActivity(i);
                finish();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }
}
