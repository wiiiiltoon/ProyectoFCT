package com.example.aplicaciongestindealumnos;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class registroAlumnos extends AppCompatActivity {
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
        setContentView(R.layout.activity_registro_alumnos);

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
        adaptador = new Adaptador(registroAlumnos.this, listaAlumnos);
        listViewAlumnos.setAdapter(adaptador);
        storageRef.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
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
                // Muestra un cuadro de diálogo preguntando si se desea eliminar el alumno
                AlertDialog.Builder builder = new AlertDialog.Builder(registroAlumnos.this);
                builder.setTitle("Eliminar alumno");
                builder.setMessage("¿Está seguro que desea eliminar este alumno?");

                builder.setPositiveButton("Eliminar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Alumno alumnoSeleccionado = (Alumno) adaptador.getItem(position);
                        String idAlumno = alumnoSeleccionado.getIdFireBase();
                        DocumentReference alumnoRef = db.collection("users").document(emailDB).collection("alumnos").document(idAlumno);
                        alumnoRef.delete()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getApplicationContext(), "Agregado correctamente", Toast.LENGTH_SHORT).show();

                                        // Obtiene el elemento seleccionado y lo elimina del array y del adaptador
                                        adaptador.remove(alumnoSeleccionado);
                                        adaptador.notifyDataSetChanged();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getApplicationContext(), "No se pudo eliminar el alumno", Toast.LENGTH_SHORT).show();
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
        alumnosRefDB = db.collection("users").document(emailDB).collection("alumnos");
    }

    public void anadirAlumno(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(registroAlumnos.this);
        builder.setTitle("Añadir alumno");
        builder.setMessage("Indique los datos del alumno:");

        // Boton para añadir imagen
        Button botonImagen = new Button(registroAlumnos.this);
        botonImagen.setText("Seleccionar imagen");

        // Campo de nombre y apellidos
        EditText campoNombre = new EditText(registroAlumnos.this);
        campoNombre.setHint("Nombre y apellidos");

        // Campo para añdir el año del curso
        EditText campoAñoCurso = new EditText(registroAlumnos.this);
        campoAñoCurso.setHint("Año del curso");

        // Campo desplegable de Nivel Educativo
        String[] nivelesEducativos = {"ESO", "BACHILLER", "GRADO MEDIO", "GRADO SUPERIOR", "FP MEDIO", "FP SUPERIOR"};
        Spinner spinnerNivelEducativo = new Spinner(registroAlumnos.this);
        ArrayAdapter<String> adapterNivelEducativo = new ArrayAdapter<>(registroAlumnos.this, android.R.layout.simple_spinner_dropdown_item, nivelesEducativos);

        // Establece el adaptador para el spinner
        spinnerNivelEducativo.setAdapter(adapterNivelEducativo);

        EditText campoAsignatura = new EditText(registroAlumnos.this);
        campoAsignatura.setHint("Asignatura");

        // Creamos un layout para los campos de texto
        LinearLayout layoutCampos = new LinearLayout(registroAlumnos.this);
        layoutCampos.setOrientation(LinearLayout.VERTICAL);
        layoutCampos.setGravity(Gravity.CENTER);
        layoutCampos.setPadding(50, 50, 50, 50);

        // Agregamos los componentes al layout
        uriElegidaCliente = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.logo);
        imagenSeleccionada = new ImageView(registroAlumnos.this);
        imagenSeleccionada.setImageResource(R.drawable.logo);
        imagenSeleccionada.setLayoutParams(new LinearLayout.LayoutParams(500, 500));
        imagenSeleccionada.setAdjustViewBounds(true);


        layoutCampos.addView(imagenSeleccionada);
        layoutCampos.addView(botonImagen);
        layoutCampos.addView(campoNombre);
        layoutCampos.addView(campoAñoCurso);
        layoutCampos.addView(spinnerNivelEducativo);
        layoutCampos.addView(campoAsignatura);

        // Agregamos el layout al diálogo
        builder.setView(layoutCampos);

        botonImagen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 1);

            }
        });

        // agregamos botones al cuadro de dialogo y creamos una accion
        builder.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //Recogemos los datos
                String nombre = campoNombre.getText().toString().replaceAll("\\s+", " ").trim();
                String anio = campoAñoCurso.getText().toString().replaceAll("\\s+", " ").trim();
                String asignatura = campoAsignatura.getText().toString().replaceAll("\\s+", " ").trim();
                String nivelEducativo = spinnerNivelEducativo.getSelectedItem().toString();
                String curso = anio + "º " + nivelEducativo;

                if (nombre.equals("") && anio.equals("") && asignatura.equals("")) {
                    Toast.makeText(getApplicationContext(), "Complete los campos requeridos", Toast.LENGTH_SHORT).show();
                } else {
                    //Generamos una nueva ID para la base de datos
                    String nuevoID = alumnosRefDB.document().getId();
                    // Crear una referencia al archivo en Firebase Storage
                    Date date = new Date();
                    String timestamp = String.valueOf(date.getTime());
                    String nombreImagen = "alumno_" + timestamp + ".jpg";
                    StorageReference imageRef = storageRef.child(nombreImagen);

                    // Subir la imagen a Firebase Storage
                    UploadTask uploadTask = imageRef.putFile(uriElegidaCliente);

                    // Manejar el resultado de la carga
                    uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // La imagen se ha cargado con éxito
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri downloadUrl) {
                                    // La URL de descarga de la imagen está disponible
                                    String urlImagen = downloadUrl.toString();


                                    //Creamos el map
                                    Map<String, Object> nuevoAlumnoMap = new HashMap<>();
                                    nuevoAlumnoMap.put("nombre", nombre);
                                    nuevoAlumnoMap.put("curso", curso);
                                    nuevoAlumnoMap.put("url_imagen", urlImagen);

                                    // Añadir el nuevo alumno a la colección
                                    alumnosRefDB.document(nuevoID).set(nuevoAlumnoMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    // Añadimos un alumno a la lista local
                                                    listaAlumnos.add(new Alumno(uriElegidaCliente, nombre, curso, nuevoID));

                                                    // Crear una instancia del adaptador con la lista de alumnos y establecerlo en la lista
                                                    adaptador = new Adaptador(registroAlumnos.this, listaAlumnos);
                                                    listViewAlumnos.setAdapter(adaptador);
                                                    Toast.makeText(getApplicationContext(), "Alumno agregado con exito", Toast.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Toast.makeText(getApplicationContext(), "Error al agregar alumno a Firestore", Toast.LENGTH_SHORT).show();

                                                }
                                            });
                                }
                            });
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getApplicationContext(), "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            uriElegidaCliente = data.getData();
            Toast.makeText(getApplicationContext(), uriElegidaCliente.toString(), Toast.LENGTH_SHORT).show();
            imagenSeleccionada.setImageURI(uriElegidaCliente);
        }
    }

    //Recorte de imagen cuando se selecciona una (proceso lento)
    /* @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            // Configuramos uCrop
            UCrop.Options options = new UCrop.Options();
            options.setCompressionQuality(70);
            options.setToolbarTitle("Recortar imagen");
            options.setStatusBarColor(getResources().getColor(R.color.titulo));
            options.setToolbarColor(getResources().getColor(R.color.azulFondo));
            options.setActiveControlsWidgetColor(getResources().getColor(R.color.blanco));

            // Iniciamos la actividad de uCrop
            UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), "cropped")))
                    .withAspectRatio(3, 4)
                    .withOptions(options)
                    .start(this);
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK) {
            uriElegidaCliente = UCrop.getOutput(data);

            // Aquí puedes hacer algo con la imagen recortada, como subirla a Firebase Storage o mostrarla en una ImageView
            imagenSeleccionada.setImageURI(uriElegidaCliente);
        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable error = UCrop.getError(data);

            // Aquí puedes manejar el error
        }
    }*/
}
