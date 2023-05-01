package com.example.aplicaciongestindealumnos;

import androidx.annotation.NonNull;
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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Registro extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private EditText correo;
    private EditText contraseña;
    private EditText confirmarContraseña;
    private EditText nombre;
    private EditText centro;
    private TextView fraseIniciarSesion;
    private SpannableString linkIniciarSesion;
    private ClickableSpan clickEnElLink;
    private CheckBox mostrarContraseña;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        correo = findViewById(R.id.campoCorreo);
        contraseña = findViewById(R.id.campoContraseña);
        confirmarContraseña = findViewById(R.id.campoConfirmarContraseña);
        nombre = findViewById(R.id.campoNombre);
        centro = findViewById(R.id.campoCentro);
        mostrarContraseña = findViewById(R.id.checkMostrarContraseña);
        fraseIniciarSesion = findViewById(R.id.fraseIniciarSesion);

        // Link a iniciar sesion
        linkIniciarSesion = new SpannableString("Iniciar sesión");
        clickEnElLink = new ClickableSpan() {
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
        mostrarContraseña.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    contraseña.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    confirmarContraseña.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    contraseña.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    confirmarContraseña.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    public void RegistrarUsuario(View view) {

        if (contraseña.getText().toString().trim().equals(confirmarContraseña.getText().toString().trim())) {
            mAuth.createUserWithEmailAndPassword(correo.getText().toString().trim(), contraseña.getText().toString().trim())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.e("Registro", "Error al crear usuario", task.getException());
                            if (task.isSuccessful()) {

                                //guardar datos en un maps para la base de datos
                                Map<String, Object> datosProfe = new HashMap<>();
                                datosProfe.put("nombre", nombre.getText().toString());
                                datosProfe.put("centro", centro.getText().toString());


                                //Creamos la refencia a la coleccion
                                db.collection("users").document(correo.getText().toString()).set(datosProfe)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                //guardado con exito
                                                Log.d("Agregacion", "Datos guardados con exito");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.d("Agregacion", "Error al agregar en la base de datos");
                                            }
                                        });

                                Toast.makeText(getApplicationContext(), "Usuario creado", Toast.LENGTH_SHORT).show();
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                //updateUI(user);
                                Intent i = new Intent(getApplicationContext(), inicioSesion.class);
                                startActivity(i);
                            } else {
                                Log.e("Registro", "Error al crear usuario: ", task.getException());
                                // If sign in fails, display a message to the user.
                                //Log.w(TAG, "signInWithCustomToken:failure", task.getException());
                                Toast.makeText(getApplicationContext(), "Authentication failed.", Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }
                        }
                    });
        } else {
            Toast.makeText(this, "Las contraseñas no coinciden", Toast.LENGTH_SHORT).show();
        }


    }
}