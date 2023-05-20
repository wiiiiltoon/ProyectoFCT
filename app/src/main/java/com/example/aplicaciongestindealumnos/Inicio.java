package com.example.aplicaciongestindealumnos;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class Inicio extends AppCompatActivity {

    private FirebaseAuth mAuth;
    DocumentReference referenciaCorreo;
    String emailDB;
    TextView datoNombre, datoCentro;
    Button consulta, registro, calendario, informe, cerrarSesion,asignaturas;
    ArrayList<Alumno> listaAlumnos;
    ArrayList<String> listaAsignaturas;
    ArrayList<NotaCalendario> listaNotasCalendario;
    CollectionReference alumnosRefDB, asignaturasRefDB,notasCalendarioRefDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        consulta = findViewById(R.id.botonConsulta);
        registro = findViewById(R.id.botonRegistro);
        asignaturas = findViewById(R.id.botonAsignaturas);
        calendario = findViewById(R.id.botonCalendario);
        informe = findViewById(R.id.botonInforme);
        cerrarSesion = findViewById(R.id.cerrarSesion);
        datoNombre = findViewById(R.id.datoNombre);
        datoCentro = findViewById(R.id.datoCentro);
        listaAlumnos = new ArrayList<>();
        listaAsignaturas = new ArrayList<>();
        listaNotasCalendario = new ArrayList<>();


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
            Log.w("Inicio", "El usuario actual es nulo");
        }
        //hacemos referencia a las colleciones
        alumnosRefDB = db.collection("users").document(emailDB).collection("alumnos");
        asignaturasRefDB = db.collection("users").document(emailDB).collection("asignaturas");
        notasCalendarioRefDB = db.collection("users").document(emailDB).collection("notasCalendario");
        // Cargar en lista los alumnos ya registrados en la base de datos
        alumnosRefDB.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Clase: Inicio.java", "Error al escuchar los cambios en la base de datos.", error);
                    return;
                }
                cargarAlumnos();
            }
        });
        asignaturasRefDB.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Clase: Inicio.java", "Error al escuchar los cambios en la base de datos.", error);
                    return;
                }
                cargarAsignaturas();
            }
        });
        notasCalendarioRefDB.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e("Clase: Inicio.java", "Error al escuchar los cambios en la base de datos.", error);
                    return;
                }
                cargarNotasCalendario();
            }
        });
    }

    private void cargarAsignaturas() {
        listaAsignaturas.clear();
        asignaturasRefDB.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String idAsignatura = document.getId();
                        listaAsignaturas.add(idAsignatura);
                    }
                }
            }
        });
    }

    public void cerrarSesion(View view) {
        mostrarCerrarSesion();
    }
    public void RegistroAlumnos(View view) {
        Intent i = new Intent(Inicio.this, RegistroAlumnos.class);
        i.putExtra("listaAlumnos", listaAlumnos);
        i.putExtra("correoUsuario",emailDB);
        startActivity(i);
    }
    public void ConsultarAlumnos(View view) {
        Intent i = new Intent(Inicio.this, ConsultarAlumnos.class);
        i.putExtra("listaAlumnos", listaAlumnos);
        i.putExtra("correoUsuario",emailDB);
        startActivity(i);
    }
    public void crearAsignaturas(View view) {
        Intent i = new Intent(Inicio.this,RegistroAsignaturas.class);
        i.putStringArrayListExtra("listaAsignaturas", listaAsignaturas);
        i.putExtra("correoUsuario",emailDB);
        startActivity(i);
    }
    public void Calendario(View view) {
        Intent i = new Intent(Inicio.this, Calendario.class);
        i.putExtra("listaNotasCalendario", listaNotasCalendario);
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
            Intent i = new Intent(Inicio.this, InicioSesion.class);
            startActivity(i);
            finish();
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }
    private void cargarAlumnos() {
        listaAlumnos.clear();
        alumnosRefDB.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    // Recorrer los documentos y obtener los datos de cada alumno
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // Obtener los datos del documento y convertirlos a un objeto Alumno
                        String idFireBase = document.getId();
                        String nombre = document.getString("nombre");
                        String curso = document.getString("curso");
                        String urlImagen = document.getString("url_imagen");
                        Uri uriUrlImagen = Uri.parse(urlImagen);
                        listaAlumnos.add(new Alumno(uriUrlImagen, nombre, curso,idFireBase));
                    }
                }
            }
        });
    }
    private void cargarNotasCalendario() {
        listaNotasCalendario.clear();
        notasCalendarioRefDB.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String fecha = document.getId();
                        String nota = document.getString("nota");
                        listaNotasCalendario.add(new NotaCalendario(fecha,nota));
                    }
                }
            }
        });
    }
}
