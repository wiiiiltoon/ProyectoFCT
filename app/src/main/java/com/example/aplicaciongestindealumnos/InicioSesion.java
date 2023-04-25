package com.example.aplicaciongestindealumnos;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class InicioSesion extends AppCompatActivity {

    Button registro;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        registro = (Button) findViewById(R.id.botonRegistro);
        registro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(InicioSesion.this, Registro.class);
                startActivity(i);
            }
        });
    }
}