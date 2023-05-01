package com.example.aplicaciongestindealumnos;

import androidx.appcompat.app.AppCompatActivity;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class Registro extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText mContrasena;
    private EditText mConfirmarContrasena;
    private EditText mNombre;
    private EditText mCentro;
    String correo, contrasena, confirmarContrasena, nombre, centro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        EditText mCorreo = findViewById(R.id.campoCorreo);
        mContrasena = findViewById(R.id.campoContrasena);
        mConfirmarContrasena = findViewById(R.id.campoConfirmarContrasena);
        mNombre = findViewById(R.id.campoNombre);
        mCentro = findViewById(R.id.campoCentro);
        CheckBox mostrarContrasena = findViewById(R.id.checkMostrarContrasena);
        TextView fraseIniciarSesion = findViewById(R.id.fraseIniciarSesion);

        correo = mCorreo.getText().toString().trim();
        contrasena = mContrasena.getText().toString().trim();
        confirmarContrasena = mConfirmarContrasena.getText().toString().trim();
        nombre = mNombre.getText().toString().trim();
        centro = mCentro.getText().toString().trim();

        // Link a iniciar sesion
        SpannableString linkIniciarSesion = new SpannableString("Iniciar sesión");
        ClickableSpan clickEnElLink = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Registro.this, inicioSesion.class);
                startActivity(i);
            }
        };

        // Concatenacion al texto en el registro
        linkIniciarSesion.setSpan(clickEnElLink, 0, linkIniciarSesion.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        fraseIniciarSesion.append(linkIniciarSesion);
        fraseIniciarSesion.setMovementMethod(LinkMovementMethod.getInstance());

        // Checkbox mostrar contraseña
        mostrarContrasena.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                //Mostrar contraseña
                mContrasena.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                mConfirmarContrasena.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
            } else {
                //Ocultar contraseña
                mContrasena.setTransformationMethod(PasswordTransformationMethod.getInstance());
                mConfirmarContrasena.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    public void RegistrarUsuario(View view) {

        if (contrasena.equals(confirmarContrasena)) {
            mAuth.createUserWithEmailAndPassword(correo, contrasena)
                    .addOnCompleteListener(this, task -> {
                        Log.e("Registro", "Error al crear usuario", task.getException());
                        if (task.isSuccessful()) {

                            //guardar datos en un maps para la base de datos
                            Map<String, Object> datosProfe = new HashMap<>();
                            datosProfe.put("nombre", mNombre.getText().toString());
                            datosProfe.put("centro", mCentro.getText().toString());

                            //Creamos la refencia a la coleccion
                            db.collection("users").document(correo).set(datosProfe)
                                    .addOnSuccessListener(aVoid -> {
                                        //guardado con exito
                                        Log.d("Agregacion", "Datos guardados con exito");
                                    })
                                    .addOnFailureListener(e -> Log.d("Agregacion", "Error al agregar en la base de datos"));

                            Toast.makeText(getApplicationContext(), "Usuario creado", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(getApplicationContext(), inicioSesion.class);
                            startActivity(i);
                        } else {
                            Log.e("Registro", "Error al crear usuario: ", task.getException());
                            Toast.makeText(getApplicationContext(), "Verifique su correo electronico.", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
        }


    }
}