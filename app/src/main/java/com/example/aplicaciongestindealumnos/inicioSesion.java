package com.example.aplicaciongestindealumnos;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class inicioSesion extends AppCompatActivity {
    private FirebaseAuth mAuth;

    EditText mCorreo, mContrasena;
    Button inicioSesion, registrarse;
    String correo, contrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciosesion);
        mAuth = FirebaseAuth.getInstance();
        mCorreo = findViewById(R.id.campoCorreo);
        mContrasena = findViewById(R.id.campoContrasena);
        inicioSesion = findViewById(R.id.botonLogin);
        registrarse = findViewById(R.id.botonRegistro);

        correo = mCorreo.getText().toString().trim();
        contrasena = mContrasena.getText().toString().trim();
        registrarse.setOnClickListener(view -> {
            Intent i = new Intent(com.example.aplicaciongestindealumnos.inicioSesion.this, Registro.class);
            startActivity(i);
        });

    }

    public void iniciarSesion(View view) {

        mAuth.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Sesion iniciada correctamente", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(com.example.aplicaciongestindealumnos.inicioSesion.this, principal.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(getApplicationContext(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}