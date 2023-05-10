package com.example.aplicaciongestindealumnos;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.parceler.Parcels;

import java.util.ArrayList;

public class principal extends AppCompatActivity {

    private FirebaseAuth mAuth;
    DocumentReference referenciaCorreo;
    String emailDB;
    TextView datoNombre, datoCentro;
    Button consulta, registro, calificaciones, ausencias, calendario, informe, cerrarSesion;
    ArrayList<Alumno> listaAlumnos;
    CollectionReference alumnosRefDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        consulta = findViewById(R.id.botonConsulta);
        registro = findViewById(R.id.botonRegistro);
        calificaciones = findViewById(R.id.botonCalificaciones);
        ausencias = findViewById(R.id.botonAusencias);
        calendario = findViewById(R.id.botonCalendario);
        informe = findViewById(R.id.botonInforme);
        cerrarSesion = findViewById(R.id.cerrarSesion);
        datoNombre = findViewById(R.id.datoNombre);
        datoCentro = findViewById(R.id.datoCentro);
        listaAlumnos = new ArrayList<>();


        //Instancia para recoger el correo
        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = SingletonFirebase.getFireBase();
        //Obtenemos el email
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            emailDB = currentUser.getEmail();

            //hacemos referencia a al correo
            referenciaCorreo = db.collection("users").document(emailDB);
            referenciaCorreo.get().addOnSuccessListener(documentSnapshot -> {
                //si existe el datos con el email
                if (documentSnapshot.exists()) {
                    String nombreDB = documentSnapshot.getString("nombre");
                    String centroDB = documentSnapshot.getString("centro");
                    String infoNombre = getString(R.string.nombre_mas_string, nombreDB);
                    datoNombre.setText(infoNombre);
                    String infoCentro = getString(R.string.centro_mas_string, centroDB);
                    datoCentro.setText(infoCentro);
                }
            });
        } else {
            Log.w("principal", "El usuario actual es nulo");
        }
        //hacemos referencia a la collecion
        alumnosRefDB = db.collection("users").document(emailDB).collection("alumnos");
        // Cargar em lista los alumnos ya registrados en la base de datos
        alumnosRefDB.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Recorrer los documentos y obtener los datos de cada alumno
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Obtener los datos del documento y convertirlos a un objeto Alumno
                        String nombre = document.getString("nombre");
                        String curso = document.getString("curso");
                        String urlImagen = document.getString("url_imagen");
                        Uri uriUrlImagen = Uri.parse(urlImagen);

                        listaAlumnos.add(new Alumno(uriUrlImagen, nombre, curso));
                    }
                }

            }
        });
    }

    public void cerrarSesion(View view) {
        mostrarCerrarSesion();
    }
    public void registroAlumnos(View view) {
        Intent i = new Intent(principal.this,registroAlumnos.class);
        i.putExtra("listaAlumnos", listaAlumnos);
        i.putExtra("correoUsuario",emailDB);
        startActivity(i);
    }
    public void consultarAlumnos(View view) {
        Intent i = new Intent(principal.this,consultarAlumnos.class);
        i.putExtra("listaAlumnos", listaAlumnos);
        i.putExtra("correoUsuario",emailDB);
        startActivity(i);
    }

    @Override
    public void onBackPressed() {
        mostrarCerrarSesion();
    }

    private void mostrarCerrarSesion() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Cerrar sesión");
        builder.setMessage("¿Estás seguro de que quieres cerrar sesión?");
        builder.setPositiveButton("Sí", (dialog, which) -> {
            mAuth.signOut();
            Intent i = new Intent(principal.this, inicioSesion.class);

            startActivity(i);
            finish();
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }
}
