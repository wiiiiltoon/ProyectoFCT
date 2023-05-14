package com.example.aplicaciongestindealumnos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;


public class consultarAlumnos extends AppCompatActivity {
    FirebaseFirestore db;
    FirebaseStorage storage;
    CollectionReference alumnosRefDB;
    Uri uriElegidaCliente;
    StorageReference storageRef;
    ArrayList<Alumno> listaAlumnos;
    ListView listViewAlumnos;
    Adaptador adaptador;
    ImageView imagenSeleccionada;
    String emailDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_consultar_alumnos);

        listaAlumnos = new ArrayList<>();
        listViewAlumnos = findViewById(R.id.listaAlumnos);

        Intent i = getIntent();
        emailDB = i.getStringExtra("correoUsuario");


        //llamos al singleton para instanciar
        db = SingletonFirebase.getFireBase();

        storage = SingletonFirebase.getStorage();
        storageRef = SingletonFirebase.getReferenciaFotos();


        // Crear una instancia del adaptador con la lista de alumnos traida desde principal
        listaAlumnos = i.getParcelableArrayListExtra("listaAlumnos");
        adaptador = new Adaptador(consultarAlumnos.this, listaAlumnos);
        listViewAlumnos.setAdapter(adaptador);
        storageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                // La referencia ya existe, no hay nada más que hacer
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // La referencia no existe, intenta crearla
                if (exception instanceof StorageException && ((StorageException) exception).getErrorCode() == StorageException.ERROR_OBJECT_NOT_FOUND) {
                    storageRef.putBytes(new byte[]{0}).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // La referencia se creó exitosamente
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // No se pudo crear la referencia
                        }
                    });
                }
            }
        });

        // En caso de hacer clic en un elemento de la lista
        listViewAlumnos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Alumno alumnoSeleccionado = listaAlumnos.get(position);
                String nombreAlumno = alumnoSeleccionado.getNombre();
                String cursoAlumno = alumnoSeleccionado.getCurso();
                String idAlumno = alumnoSeleccionado.getIdFireBase();
                Uri foto = alumnoSeleccionado.getUrlFoto();


                Intent i = new Intent(consultarAlumnos.this,perfilAlumno.class);
                i.putExtra("nombreAlumno", nombreAlumno);
                i.putExtra("cursoAlumno",cursoAlumno);
                i.putExtra("idAlumno",idAlumno);
                i.putExtra("fotoAlumno",foto.toString());
                startActivity(i);
                Toast.makeText(getApplicationContext(), foto.toString(), Toast.LENGTH_SHORT).show();
            }
        });
        alumnosRefDB = db.collection("users").document(emailDB).collection("alumnos");
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            uriElegidaCliente = data.getData();
            imagenSeleccionada.setImageURI(uriElegidaCliente);
        }
    }
}
