package com.example.aplicaciongestindealumnos;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

public class InicioSesion extends AppCompatActivity {
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
        CheckBox mostrarContrasena = findViewById(R.id.checkMostrarContrasena);

        registrarse.setOnClickListener(view -> {
            Intent i = new Intent(InicioSesion.this, RegistroAplicacion.class);
            startActivity(i);
        });


        // Checkbox mostrar contraseña
        mostrarContrasena.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                //Mostrar contraseña
                mContrasena.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                //Ocultar contraseña
                mContrasena.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
    }

    public void iniciarSesion(View view) {
        correo = mCorreo.getText().toString().trim();
        contrasena = mContrasena.getText().toString().trim();

        mAuth.signInWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getApplicationContext(), "Sesion iniciada correctamente", Toast.LENGTH_SHORT).show();
                        Intent i = new Intent(InicioSesion.this, Inicio.class);
                        startActivity(i);
                    } else {
                        Toast.makeText(getApplicationContext(), "Credenciales incorrectas", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}