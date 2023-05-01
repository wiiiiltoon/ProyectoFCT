package com.example.aplicaciongestindealumnos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class inicioSesion extends AppCompatActivity {
    private FirebaseAuth mAuth;

    EditText correo;
    EditText contraseña;
    Button inicioSesion;
    Button registrarse;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciosesion);
        mAuth = FirebaseAuth.getInstance();
        correo= findViewById(R.id.campoCorreo);
        contraseña = findViewById(R.id.campoContraseña);
        inicioSesion = findViewById(R.id.botonLogin);
        registrarse = findViewById(R.id.botonRegistro);

        registrarse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(com.example.aplicaciongestindealumnos.inicioSesion.this, Registro.class);
                startActivity(i);
            }
        });

    }

    public void iniciarSesion(View view) {

        mAuth.signInWithEmailAndPassword(correo.getText().toString().trim(),contraseña.getText().toString().trim())
        .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    // Sign in success, update UI with the signed-in user's information
                    FirebaseUser user = mAuth.getCurrentUser();
                    Toast.makeText(getApplicationContext(),"Sesion iniciada correctamente",Toast.LENGTH_SHORT).show();
                    Intent i = new Intent(com.example.aplicaciongestindealumnos.inicioSesion.this, principal.class);
                    startActivity(i);
                } else {
                    // If sign in fails, display a message to the user.
                    Toast.makeText(getApplicationContext(),"Credenciales incorrectas",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}