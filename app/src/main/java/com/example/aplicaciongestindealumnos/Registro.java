package com.example.aplicaciongestindealumnos;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.method.HideReturnsTransformationMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

public class Registro extends AppCompatActivity {

    CheckBox mostrarContraseña;
    EditText campoContraseña;
    EditText campoRepetirContraseña;
    TextView fraseIniciarSesion;
    SpannableString linkIniciarSesion;
    ClickableSpan clickEnElLink;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

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
        mostrarContraseña = findViewById(R.id.checkMostrarContraseña);
        campoContraseña = findViewById(R.id.campoContraseña);
        campoRepetirContraseña = findViewById(R.id.campoModiContraseña);

        mostrarContraseña.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    campoContraseña.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    campoRepetirContraseña.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                } else {
                    campoContraseña.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    campoRepetirContraseña.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
            }
        });

    }
}