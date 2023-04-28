package com.example.aplicaciongestindealumnos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.HideReturnsTransformationMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Registro extends AppCompatActivity {

    private FirebaseAuth mAuth;
    CheckBox mostrarContraseña;
    EditText correo;
    EditText contraseña;
    EditText confirmarContraseña;
    TextView fraseIniciarSesion;
    SpannableString linkIniciarSesion;
    ClickableSpan clickEnElLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        mAuth = FirebaseAuth.getInstance();

        correo = findViewById(R.id.campoCorreo);
        contraseña = findViewById(R.id.campoContraseña);
        confirmarContraseña = findViewById(R.id.campoConfirmarContraseña);
        mostrarContraseña = findViewById(R.id.checkMostrarContraseña);

        //Link a iniciar sesion
        fraseIniciarSesion = findViewById(R.id.fraseIniciarSesion);
        linkIniciarSesion = new SpannableString("Iniciar sesión");
        clickEnElLink = new ClickableSpan() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Registro.this, InicioSesion.class);
                startActivity(i);
            }
        };
        // concatenacion al texto en el registro
        linkIniciarSesion.setSpan(clickEnElLink, 0, linkIniciarSesion.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        fraseIniciarSesion.append(linkIniciarSesion);
        fraseIniciarSesion.setMovementMethod(LinkMovementMethod.getInstance());

        //checkbox mostrar contraseña
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
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);


    }
    public void RegistrarUsuario(View view){
        Log.d("Contraseña", "Contraseña: " + contraseña.getText().toString().trim());
        Log.d("Contraseña", "Confirmar contraseña: " + confirmarContraseña.getText().toString().trim());

        if (contraseña.getText().toString().trim().equals(confirmarContraseña.getText().toString().trim())) {
            mAuth.createUserWithEmailAndPassword(correo.getText().toString().trim(),contraseña.getText().toString().trim())
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.e("Registro", "Error al crear usuario", task.getException());
                            if (task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(),"Usuario creado",Toast.LENGTH_SHORT).show();
                                // Sign in success, update UI with the signed-in user's information
                                FirebaseUser user = mAuth.getCurrentUser();
                                //updateUI(user);
                                Intent i = new Intent(getApplicationContext(), InicioSesion.class);
                                startActivity(i);
                            } else {
                                Log.e("Registro", "Error al crear usuario: ", task.getException());
                                // If sign in fails, display a message to the user.
                                //Log.w(TAG, "signInWithCustomToken:failure", task.getException());
                                Toast.makeText(getApplicationContext(),"Authentication failed.",Toast.LENGTH_SHORT).show();
                                //updateUI(null);
                            }
                        }
                    });
        }else{
            Toast.makeText(this,"Las contraseñas no coinciden",Toast.LENGTH_SHORT).show();
        }


    }
}