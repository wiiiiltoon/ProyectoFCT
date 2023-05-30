package com.example.aplicaciongestindealumnos;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistroAplicacion extends AppCompatActivity {

    private CheckBox checkContraseña;
    private TextView fraseIniciarSesion;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText campoNombre, campoCentro, campoCorreo, campoContrasena, campoConfirmarContrasena;
    private String correo, contrasena, confirmarContrasena, nombre, centro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registro_aplicacion);

        relacionXML();
        inicializarFirebase();
        linkIniciarSesion();
        accionMostrarContraseña();
    }

    private void relacionXML() {
        campoCorreo = findViewById(R.id.campoCorreo);
        campoContrasena = findViewById(R.id.campoContrasena);
        campoConfirmarContrasena = findViewById(R.id.campoConfirmarContrasena);
        campoNombre = findViewById(R.id.campoNombre);
        campoCentro = findViewById(R.id.campoCentro);
        checkContraseña = findViewById(R.id.checkMostrarContrasena);
        fraseIniciarSesion = findViewById(R.id.fraseIniciarSesion);
    }

    private void inicializarFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void linkIniciarSesion() {
        SpannableString linkIniciarSesion = new SpannableString("Iniciar sesión");
        ClickableSpan textoClicable = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RegistroAplicacion.this, InicioSesion.class);
                startActivity(i);
            }
        };
        linkIniciarSesion.setSpan(textoClicable, 0, linkIniciarSesion.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        fraseIniciarSesion.append(linkIniciarSesion);
        fraseIniciarSesion.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void accionMostrarContraseña() {
        checkContraseña.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                mostrarContraseña();
            } else {
                ocultarContraseña();
            }
        });
    }

    private void mostrarContraseña() {
        campoContrasena.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
        campoConfirmarContrasena.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
    }

    private void ocultarContraseña() {
        campoContrasena.setTransformationMethod(PasswordTransformationMethod.getInstance());
        campoConfirmarContrasena.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void RegistrarUsuario(View view) {
        obtenerValoresCampos();

        if (nombre.isEmpty() || centro.isEmpty() || correo.isEmpty())
            mostrarMensaje("Debe rellenar los campos obligatorios (*)");
        else {
            if (contrasena.equals(confirmarContrasena)) {
                if (validarContraseña(contrasena)) {
                    verificarCorreoRegistrado(correo);
                }
            } else {
                mostrarMensaje("Las contraseñas no coinciden");
            }
        }
    }

    private void obtenerValoresCampos() {
        correo = campoCorreo.getText().toString().trim();
        contrasena = campoContrasena.getText().toString().trim();
        confirmarContrasena = campoConfirmarContrasena.getText().toString().trim();
        nombre = campoNombre.getText().toString().trim();
        centro = campoCentro.getText().toString().trim();
    }

    private boolean validarContraseña(String contraseña) {
        if (contraseña.length() < 6) {
            mostrarMensaje("La contraseña debe contener al menos 6 caracteres");
            return false;
        }

        boolean tieneLetras = false;
        boolean tieneNumeros = false;

        for (char c : contraseña.toCharArray()) {
            if (Character.isLetter(c)) {
                tieneLetras = true;
            } else if (Character.isDigit(c)) {
                tieneNumeros = true;
            }
        }

        if (tieneLetras && tieneNumeros) {
            return true;
        } else {
            mostrarMensaje("La contraseña debe contener al menos 1 letra y 1 nuemero");
            return false;
        }
    }

    private void verificarCorreoRegistrado(String correo) {
        FirebaseAuth.getInstance().fetchSignInMethodsForEmail(correo)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<String> signInMethods = task.getResult().getSignInMethods();
                        if (signInMethods != null && !signInMethods.isEmpty()) {
                            mostrarMensaje("El correo ya está registrado");
                        } else {
                            crearUsuarioEnFirebase();
                        }
                    } else {
                        mostrarMensaje("Error al verificar el correo");
                    }
                });
    }

    private void crearUsuarioEnFirebase() {
        mAuth.createUserWithEmailAndPassword(correo, contrasena)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        guardarDatosEnFirestore();
                        mostrarMensaje("Usuario creado");
                        iniciarSesion();
                    } else {
                        mostrarMensaje("Error al crear usuario.");
                    }
                });
    }

    private void guardarDatosEnFirestore() {
        Map<String, Object> datosProfe = new HashMap<>();
        datosProfe.put("nombre", nombre);
        datosProfe.put("centro", centro);

        db.collection("users").document(correo).set(datosProfe)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Agregacion", "Datos guardados con éxito");
                })
                .addOnFailureListener(e -> Log.d("Agregacion", "Error al agregar en la base de datos"));
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
    }

    private void iniciarSesion() {
        Intent i = new Intent(getApplicationContext(), InicioSesion.class);
        startActivity(i);
    }
}