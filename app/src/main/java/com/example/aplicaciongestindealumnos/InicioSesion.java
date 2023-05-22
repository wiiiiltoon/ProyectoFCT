package com.example.aplicaciongestindealumnos;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class InicioSesion extends AppCompatActivity {
    private FirebaseAuth mAuth;

    EditText campoCorreo, campoContrasena;
    Button botonInicioSesion, botonRegistrarse;
    String correo, contrasena;
    CheckBox mostrarContrasena;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_iniciosesion);

        mAuth = FirebaseAuth.getInstance();
        relacionXML();
        accionMostrarContraseña();
    }

    public void registrarse(View view) {
        Intent i = new Intent(InicioSesion.this, RegistroAplicacion.class);
        startActivity(i);
    }

    private void relacionXML() {
        campoCorreo = findViewById(R.id.campoCorreo);
        campoContrasena = findViewById(R.id.campoContrasena);
        botonInicioSesion = findViewById(R.id.botonLogin);
        botonRegistrarse = findViewById(R.id.botonRegistro);
        mostrarContrasena = findViewById(R.id.checkMostrarContrasena);
    }

    private void accionMostrarContraseña() {
        mostrarContrasena.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                //Mostrar contraseña
                campoContrasena.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                //Ocultar contraseña
                campoContrasena.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
    }

    public void iniciarSesion(View view) {
        correo = campoCorreo.getText().toString().trim();
        contrasena = campoContrasena.getText().toString().trim();
        verificarCredenciales(correo, contrasena);
    }

    private void verificarCredenciales(String correoUsuario, String contraseñaUsuario) {
        mAuth.signInWithEmailAndPassword(correoUsuario, contraseñaUsuario)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        mostrarMensaje("Sesion iniciada correctamente");
                        Intent i = new Intent(InicioSesion.this, Inicio.class);
                        startActivity(i);
                    } else {
                        mostrarMensaje("Credenciales incorrectas");
                    }
                });
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
    }
}