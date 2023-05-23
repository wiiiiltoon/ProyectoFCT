package com.example.aplicaciongestindealumnos;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class Inicio extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseAuth mAuth;
    DocumentReference referenciaCorreo;
    String emailDB;
    TextView datoNombre, datoCentro;
    Button consulta, registro, calendario, informe, cerrarSesion, asignaturas;
    ArrayList<Alumno> listaAlumnos;
    ArrayList<String> listaAsignaturas;
    ArrayList<NotaCalendario> listaNotasCalendario;
    CollectionReference alumnosRefDB, asignaturasRefDB, notasCalendarioRefDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        relacionXML();
        inicializarListas();
        inicializarFirebase();
        obtenerCorreoUsuario();
        cargarAlumnosDesdeDB();
        cargarAsignaturasDesdeDB();
        cargarNotasCalendarioDesdeDB();
    }

    private void relacionXML() {
        consulta = findViewById(R.id.botonConsulta);
        registro = findViewById(R.id.botonRegistro);
        asignaturas = findViewById(R.id.botonAsignaturas);
        calendario = findViewById(R.id.botonCalendario);
        informe = findViewById(R.id.botonInforme);
        cerrarSesion = findViewById(R.id.cerrarSesion);
        datoNombre = findViewById(R.id.datoNombre);
        datoCentro = findViewById(R.id.datoCentro);
    }

    private void inicializarListas() {
        listaAlumnos = new ArrayList<>();
        listaAsignaturas = new ArrayList<>();
        listaNotasCalendario = new ArrayList<>();
    }

    private void inicializarFirebase() {
        db = SingletonFirebase.getFireBase();
        mAuth = FirebaseAuth.getInstance();
    }

    private void obtenerCorreoUsuario() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            emailDB = currentUser.getEmail();
            referenciaCorreo = db.collection("users").document(emailDB);
            referenciaCorreo.get().addOnSuccessListener(documentSnapshot -> {
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
            Log.w("Inicio", "El usuario actual es nulo");
        }
    }

    private void cargarAlumnosDesdeDB() {
        alumnosRefDB = db.collection("users").document(emailDB).collection("alumnos");
        alumnosRefDB.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Clase: Inicio.java", "Error al escuchar los cambios en la base de datos.", error);
                return;
            }
            cargarAlumnos();
        });
    }

    private void cargarAsignaturasDesdeDB() {
        asignaturasRefDB = db.collection("users").document(emailDB).collection("asignaturas");
        asignaturasRefDB.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Clase: Inicio.java", "Error al escuchar los cambios en la base de datos.", error);
                return;
            }
            cargarAsignaturas();
        });
    }

    private void cargarNotasCalendarioDesdeDB() {
        notasCalendarioRefDB = db.collection("users").document(emailDB).collection("notasCalendario");
        notasCalendarioRefDB.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("Clase: Inicio.java", "Error al escuchar los cambios en la base de datos.", error);
                return;
            }
            cargarNotasCalendario();
        });
    }

    private void cargarAlumnos() {
        listaAlumnos.clear();
        alumnosRefDB.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String idFireBase = document.getId();
                    String nombre = document.getString("nombre");
                    String curso = document.getString("curso");
                    String urlImagen = document.getString("url_imagen");
                    ArrayList asignaturas = (ArrayList<String>) document.get("asignaturas");
                    Uri uriUrlImagen = Uri.parse(urlImagen);

                    listaAlumnos.add(new Alumno(uriUrlImagen, nombre, curso, idFireBase,asignaturas));
                }
            }
        });
    }

    private void cargarAsignaturas() {
        listaAsignaturas.clear();
        asignaturasRefDB.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String idAsignatura = document.getId();
                    listaAsignaturas.add(idAsignatura);
                }
            }
        });
    }

    private void cargarNotasCalendario() {
        listaNotasCalendario.clear();
        notasCalendarioRefDB.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String fecha = document.getId();
                    String nota = document.getString("nota");
                    listaNotasCalendario.add(new NotaCalendario(fecha, nota));
                }
            }
        });
    }

    public void cerrarSesion(View view) {
        mostrarCerrarSesion();
    }

    public void intentRegistroAlumnos(View view) {
        Intent i = new Intent(Inicio.this, RegistroAlumnos.class);
        i.putExtra("listaAlumnos", listaAlumnos);
        i.putStringArrayListExtra("listaAsignaturas", listaAsignaturas);
        i.putExtra("correoUsuario", emailDB);
        startActivity(i);
    }

    public void intentConsultarAlumnos(View view) {
        Intent i = new Intent(Inicio.this, ConsultarAlumnos.class);
        i.putExtra("correoUsuario", emailDB);
        i.putExtra("listaAlumnos", listaAlumnos);
        startActivity(i);
    }

    public void intentRegistroAsignaturas(View view) {
        Intent i = new Intent(Inicio.this, RegistroAsignaturas.class);
        i.putStringArrayListExtra("listaAsignaturas", listaAsignaturas);
        i.putExtra("correoUsuario", emailDB);
        startActivity(i);
    }

    public void intentCalendario(View view) {
        Intent i = new Intent(Inicio.this, Calendario.class);
        i.putExtra("listaNotasCalendario", listaNotasCalendario);
        i.putExtra("correoUsuario", emailDB);
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
            Intent i = new Intent(Inicio.this, InicioSesion.class);
            startActivity(i);
            finish();
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }
}
