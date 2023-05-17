package com.example.aplicaciongestindealumnos;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;

public class RegistroAsignaturas extends AppCompatActivity {
    FirebaseFirestore db;
    CollectionReference alumnosRefDB;
    Button botonCrear;
    EditText campoAsignatura;
    ListView listView;
    ArrayList<String> listaAsignaturas;
    ArrayAdapter<String> adaptador;
    TextView volver;
    String emailDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_asignaturas);
        listaAsignaturas = new ArrayList<>();
        db = SingletonFirebase.getFireBase();

        Intent i = getIntent();
        emailDB = i.getStringExtra("correoUsuario");
        listaAsignaturas = i.getStringArrayListExtra("listaAsignaturas");
        alumnosRefDB = db.collection("users").document(emailDB).collection("asignaturas");

        botonCrear = findViewById(R.id.botonCrear);
        campoAsignatura = findViewById(R.id.campoAsignatura);
        listView = findViewById(R.id.listaAsignaturas);
        volver = findViewById(R.id.textoVolver);
        SpannableString spannableString = new SpannableString("Volver");
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        volver.setText(spannableString);


        adaptador = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, listaAsignaturas);
        listView.setAdapter(adaptador);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Muestra un cuadro de diálogo preguntando si se desea eliminar el alumno
                AlertDialog.Builder builder = new AlertDialog.Builder(RegistroAsignaturas.this);
                builder.setTitle("Eliminar asignatura");
                builder.setMessage("¿Está seguro que desea eliminar esta asignatura?");

                builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String nombreAsignatura = adaptador.getItem(position);
                        DocumentReference asignaturaRef = db.collection("users").document(emailDB).collection("asignaturas").document(nombreAsignatura);
                        asignaturaRef.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(), "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                                        adaptador.remove(nombreAsignatura);
                                        adaptador.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "No se pudo eliminar la asignatura", Toast.LENGTH_SHORT).show();
                                    }
                                });

                    }
                });

                builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });
    }
    public void volverAtras(View view) {
        // Acción de volver atrás
        onBackPressed();
    }
    public void crearAsignatura(View view) {
        String nombreAsignatura = campoAsignatura.getText().toString().replaceAll("\\s+", " ").trim().trim();
        if (nombreAsignatura.equals("")) {
            Toast.makeText(getApplicationContext(), "Ingrese un nombre para la asignatura", Toast.LENGTH_SHORT).show();
        } else {
            alumnosRefDB.document(nombreAsignatura).get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {
                            if (documentSnapshot.exists()) {
                                Toast.makeText(RegistroAsignaturas.this, "La asignatura ya existe", Toast.LENGTH_SHORT).show();
                            } else {
                                // El nombre de asignatura no existe, se puede crear
                                alumnosRefDB.document(nombreAsignatura)
                                        .set(new HashMap<>(), SetOptions.merge())
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(RegistroAsignaturas.this, "Asignatura creada correctamente", Toast.LENGTH_SHORT).show();
                                                listaAsignaturas.add(nombreAsignatura);
                                                adaptador.notifyDataSetChanged();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(RegistroAsignaturas.this, "Error al crear la asignatura", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(RegistroAsignaturas.this, "Error al verificar la existencia de la asignatura", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
}