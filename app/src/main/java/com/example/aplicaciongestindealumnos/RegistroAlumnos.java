package com.example.aplicaciongestindealumnos;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class RegistroAlumnos {
    Context context;
    FirebaseFirestore db;
    FirebaseStorage storage;
    CollectionReference alumnosRefDB;
    StorageReference storageRef;
    ArrayList<Alumno> listaAlumnos;
    ArrayList<String> listaAsignaturas;
    ArrayList<String> listaAsignaturaAlumno;
    ImageView imagenRecortada;
    String emailDB;
    Uri uriElegidaCliente;
    public RegistroAlumnos(Context context, ArrayList<Alumno> listaAlumnos, ArrayList<String> listaAsignaturas, String emailDB) {
        this.context = context;
        this.listaAlumnos = listaAlumnos;
        this.listaAsignaturas = listaAsignaturas;
        this.emailDB = emailDB;
        inicializarFireBase();
    }


    private void inicializarFireBase() {
        db = SingletonFirebase.getFireBase();
        storage = SingletonFirebase.getStorage();
        alumnosRefDB = db.collection("users").document(emailDB).collection("alumnos");
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

    public ArrayList<Alumno> mostrarDialog() {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.view_registrar_alumno, null);

        imagenRecortada = view.findViewById(R.id.imagenRecortada);
        Button botonImagen = view.findViewById(R.id.botonImagen);
        EditText campoNombre = view.findViewById(R.id.campoNombre);
        EditText campoAñoCurso = view.findViewById(R.id.campoAñoCurso);
        Spinner spinnerNivelEducativo = view.findViewById(R.id.spinnerNivelEducativo);
        GridLayout contenedorAsignaturas = view.findViewById(R.id.contenedorAsignaturas);

        anadirAlumno(view, botonImagen, campoNombre, campoAñoCurso, spinnerNivelEducativo, contenedorAsignaturas);
        return listaAlumnos;
    }

    public void anadirAlumno(View view, Button botonImagen, EditText campoNombre, EditText campoAñoCurso, Spinner spinnerNivelEducativo, GridLayout contenedorAsignaturas) {
        listaAsignaturaAlumno = new ArrayList<>();
        android.app.AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Añadir alumno");
        builder.setMessage("Indique los datos del alumno:");

        builder.setView(view);

        String[] nivelesEducativos = {"ESO", "BACHILLER", "GRADO MEDIO", "GRADO SUPERIOR", "FP MEDIO", "FP SUPERIOR"};
        ArrayAdapter<String> adapterNivelEducativo = new ArrayAdapter<>(context, android.R.layout.simple_spinner_dropdown_item, nivelesEducativos);
        spinnerNivelEducativo.setAdapter(adapterNivelEducativo);

        if (listaAsignaturas.size() > 0) {
            for (String nombreAsignatura : listaAsignaturas) {
                CheckBox checkBox = new CheckBox(context);
                checkBox.setText(nombreAsignatura);
                checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        listaAsignaturaAlumno.add(nombreAsignatura);
                    } else {
                        listaAsignaturaAlumno.remove(nombreAsignatura);
                    }
                });
                contenedorAsignaturas.addView(checkBox);
            }
        }

        uriElegidaCliente = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.drawable.logoicono);

        botonImagen.setOnClickListener(v -> seleccionarImagen());

        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String nombre = obtenerTexto(campoNombre);
            String anio = obtenerTexto(campoAñoCurso);
            String nivelEducativo = spinnerNivelEducativo.getSelectedItem().toString();
            String curso = anio + "º " + nivelEducativo;

            if (nombre.isEmpty() || anio.isEmpty()) {
                mostrarMensaje("Complete los campos requeridos");
                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                try {
                    Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                    field.setAccessible(true);
                    field.set(dialog, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String nuevoID = alumnosRefDB.document().getId();
                String nombreImagen = generarNombreImagen();
                StorageReference imageRef = storageRef.child(nombreImagen);
                subirImagenFirebase(imageRef, nuevoID, nombre, curso, listaAsignaturaAlumno);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            try {
                Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
                field.setAccessible(true);
                field.set(dialog, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            dialog.dismiss();
        });

        builder.show();
    }

    private String obtenerTexto(EditText campo) {
        return campo.getText().toString().replaceAll("\\s+", " ").trim();
    }

    private String generarNombreImagen() {
        Date date = new Date();
        String timestamp = String.valueOf(date.getTime());
        return "alumno_" + timestamp + ".jpg";
    }

    private void seleccionarImagen() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        ((Activity) context).startActivityForResult(i, 1);
    }

    private void subirImagenFirebase(StorageReference imageRef, String nuevoID, String nombre, String curso, ArrayList<String> asignaturasAlumno) {
        UploadTask uploadTask = imageRef.putFile(uriElegidaCliente);

        uploadTask.addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> anadirAlumnoDB(downloadUrl, nombre, curso, nuevoID, asignaturasAlumno))).addOnFailureListener(e -> mostrarMensaje("Error al subir la imagen"));
    }

    private void anadirAlumnoDB(Uri downloadUrl, String nombre, String curso, String nuevoID, ArrayList<String> asignaturasAlumno) {
        String urlImagen = downloadUrl.toString();
        Map<String, Object> nuevoAlumnoMap = new HashMap<>();
        nuevoAlumnoMap.put("nombre", nombre);
        nuevoAlumnoMap.put("curso", curso);
        nuevoAlumnoMap.put("url_imagen", urlImagen);

        alumnosRefDB.document(nuevoID).set(nuevoAlumnoMap)
                .addOnSuccessListener(aVoid -> {
                    for (String asignatura : asignaturasAlumno) {
                        DocumentReference asignaturaRef = alumnosRefDB.document(nuevoID).collection("asignaturas").document(asignatura);
                        asignaturaRef.set(createAsignaturaData(asignatura))
                                .addOnSuccessListener(documentReference -> {
                                })
                                .addOnFailureListener(e -> mostrarMensaje("Error al agregar asignatura a Firestore"));
                    }
                    listaAlumnos.add(new Alumno(downloadUrl, nombre, curso, nuevoID, asignaturasAlumno));
                    mostrarMensaje("Alumno agregado con éxito");
                })
                .addOnFailureListener(e -> mostrarMensaje("Error al agregar alumno a Firestore"));
    }

    private Map<String, Object> createAsignaturaData(String nombreAsignatura) {
        Map<String, Object> asignaturaMap = new HashMap<>();
        asignaturaMap.put("nombre", nombreAsignatura);
        asignaturaMap.put("faltas", 0);
        return asignaturaMap;
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show();
    }
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();

            UCrop.Options options = new UCrop.Options();
            options.setCompressionQuality(70);
            options.setToolbarTitle("Recortar imagen");
            options.setStatusBarColor(ContextCompat.getColor(context, R.color.titulo));
            options.setToolbarColor(ContextCompat.getColor(context, R.color.azulFondo));
            options.setActiveControlsWidgetColor(ContextCompat.getColor(context, R.color.blanco));

            UCrop.of(uri, Uri.fromFile(new File(context.getCacheDir(), "cropped")))
                    .withAspectRatio(3, 4)
                    .withOptions(options)
                    .start((Activity) context);
        } else if (requestCode == UCrop.REQUEST_CROP && resultCode == Activity.RESULT_OK) {
            imagenRecortada.setImageDrawable(null);
            uriElegidaCliente = UCrop.getOutput(data);

            imagenRecortada.setImageURI(uriElegidaCliente);

        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable error = UCrop.getError(data);
        }
    }

}
