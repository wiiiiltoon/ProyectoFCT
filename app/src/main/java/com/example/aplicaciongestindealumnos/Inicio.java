package com.example.aplicaciongestindealumnos;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class Inicio extends AppCompatActivity {
    GestorAlumnos gestorAlumnos;
    GestorAsignaturas gestorAsignaturas;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    DocumentReference referenciaCorreo;
    String emailDB;
    TextView datoNombre, datoCentro;
    ArrayList<NotaCalendario> listaNotasCalendario;
    CollectionReference alumnosRefDB, asignaturasRefDB, notasCalendarioRefDB;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);

        gestorAlumnos = new GestorAlumnos();
        gestorAsignaturas = new GestorAsignaturas();
        relacionXML();
        inicializarListas();
        inicializarFirebase();
        obtenerCorreoUsuario();
        cargarAlumnosDesdeDB();
        cargarAsignaturasDesdeDB();
        cargarNotasCalendarioDesdeDB();
    }
    private void relacionXML() {
        datoNombre = findViewById(R.id.datoNombre);
        datoCentro = findViewById(R.id.datoCentro);
    }

    private void inicializarListas() {
        listaNotasCalendario = new ArrayList<>();
    }

    private void inicializarFirebase() {
        db = SingletonFirebase.getFireBase();
        mAuth = FirebaseAuth.getInstance();
        storageRef = SingletonFirebase.getReferenciaFotos();
        storageRef.getMetadata().addOnSuccessListener(storageMetadata -> {
        }).addOnFailureListener(exception -> {
            if (exception instanceof StorageException && ((StorageException) exception).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                storageRef.putBytes(new byte[]{0}).addOnSuccessListener(taskSnapshot -> {
                }).addOnFailureListener(exception1 -> {
                });
            }
        });
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
            Log.w("TAG", "El usuario actual es nulo");
        }
    }

    private void cargarAlumnosDesdeDB() {
        alumnosRefDB = db.collection("users").document(emailDB).collection("alumnos");
        alumnosRefDB.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String idFireBase = document.getId();
                    String nombre = document.getString("nombre");
                    String curso = document.getString("curso");
                    String urlImagen = document.getString("url_imagen");
                    Uri uriUrlImagen = Uri.parse(urlImagen);

                    obtenerAsignaturasAlumno(idFireBase, nombre, curso, uriUrlImagen);
                }
            } else {
                Log.e("TAG", "Error al obtener los alumnos desde la base de datos.", task.getException());
            }
        });
    }
    private void obtenerAsignaturasAlumno(String idFireBase, String nombre, String curso, Uri uriUrlImagen) {
        alumnosRefDB.document(idFireBase).collection("asignaturas").get().addOnCompleteListener(asignaturasTask -> {
            if (asignaturasTask.isSuccessful()) {
                ArrayList<String> asignaturas = new ArrayList<>();
                for (QueryDocumentSnapshot asignaturaDoc : asignaturasTask.getResult()) {
                    String nombreAsignatura = asignaturaDoc.getString("nombre");
                    asignaturas.add(nombreAsignatura);
                }
                gestorAlumnos.añadirAlumno(new Alumno(uriUrlImagen, nombre, curso, idFireBase, asignaturas));
            }
        });
    }
    private void cargarAsignaturasDesdeDB() {
        asignaturasRefDB = db.collection("users").document(emailDB).collection("asignaturas");
        asignaturasRefDB.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String idAsignatura = document.getId();
                    gestorAsignaturas.añadirAsignatura(idAsignatura);
                }
            }
        });
    }
    private void cargarNotasCalendarioDesdeDB() {
        notasCalendarioRefDB = db.collection("users").document(emailDB).collection("notasCalendario");
        notasCalendarioRefDB.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("TAG", "Error al escuchar los cambios en la base de datos.", error);
                return;
            }
            cargarNotasCalendario();
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

    public void intentEleccionConsultas(View view){
        Intent i = new Intent(Inicio.this, eleccion_consultas.class);
        i.putExtra("correoUsuario", emailDB);
        i.putExtra("listaAlumnos", gestorAlumnos.obtenerTodosAlumnos());
        i.putExtra("listaAsignaturas", gestorAsignaturas.obtenerTodasAsignaturas());
        startActivity(i);
    }
    public void intentEleccionRegistros(View view){
        Intent i = new Intent(Inicio.this, eleccion_registros.class);
        i.putExtra("correoUsuario", emailDB);
        i.putExtra("listaAlumnos", gestorAlumnos.obtenerTodosAlumnos());
        i.putExtra("listaAsignaturas", gestorAsignaturas.obtenerTodasAsignaturas());
        startActivity(i);
    }
    public void intentCalendario(View view) {
        Intent i = new Intent(Inicio.this, Calendario.class);
        i.putExtra("listaNotasCalendario", listaNotasCalendario);
        i.putExtra("correoUsuario", emailDB);
        startActivity(i);
    }

    private ActivityResultLauncher<Intent> launcherAlumnos = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    gestorAlumnos.setListaAlumnos(data.getParcelableArrayListExtra("alumnosDevueltos"));
                }
            }
    );
    private ActivityResultLauncher<Intent> launcherAsignaturas = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    gestorAlumnos.setListaAlumnos(data.getParcelableArrayListExtra("alumnosDevueltos"));
                }
            }
    );
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
