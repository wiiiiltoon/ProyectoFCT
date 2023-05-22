package com.example.aplicaciongestindealumnos;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class RegistroAlumnos extends AppCompatActivity {
    FirebaseFirestore db;
    FirebaseStorage storage;
    CollectionReference alumnosRefDB;
    Uri uriElegidaCliente;
    StorageReference storageRef;
    ArrayList<Alumno> listaAlumnos;
    ListView listViewAlumnos;
    AdaptadorAlumnos adaptadorAlumnos;
    ImageView imagenSeleccionada;
    ImageView imagenRecortada;
    String emailDB;
    TextView volver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_alumnos);

        relacionXML();
        accionVolver();
        recibirIntent();
        construirListaAlumnos();
        inicializarFireBase();
        accionPulsarAlumno();
    }

    private void relacionXML() {

        listViewAlumnos = findViewById(R.id.listaAlumnos);
        volver = findViewById(R.id.textoVolver);
    }

    private void accionVolver() {
        SpannableString spannableString = new SpannableString("Volver");
        spannableString.setSpan(new UnderlineSpan(), 0, spannableString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        volver.setText(spannableString);
    }

    private void recibirIntent() {
        Intent i = getIntent();
        emailDB = i.getStringExtra("correoUsuario");
        listaAlumnos = new ArrayList<>();
        listaAlumnos = i.getParcelableArrayListExtra("listaAlumnos");

    }

    private void construirListaAlumnos() {
        adaptadorAlumnos = new AdaptadorAlumnos(RegistroAlumnos.this, listaAlumnos);
        listViewAlumnos.setAdapter(adaptadorAlumnos);
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

    private void accionPulsarAlumno() {
        listViewAlumnos.setOnItemClickListener((parent, view, position, id) -> mostrarEliminarAlumno(position));
    }

    private void mostrarEliminarAlumno(final int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistroAlumnos.this);
        builder.setTitle("Eliminar alumno");
        builder.setMessage("¿Estás seguro de que quieres eliminar este alumno?");

        builder.setPositiveButton("Eliminar", (dialog, which) -> eliminarAlumno(position));
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void eliminarAlumno(int position) {
        Alumno alumnoSeleccionado = (Alumno) adaptadorAlumnos.getItem(position);
        String idAlumno = alumnoSeleccionado.getIdFireBase();
        DocumentReference alumnoRef = db.collection("users").document(emailDB).collection("alumnos").document(idAlumno);
        alumnoRef.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "Eliminado correctamente", Toast.LENGTH_SHORT).show();
                        adaptadorAlumnos.remove(alumnoSeleccionado);
                        adaptadorAlumnos.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "No se pudo eliminar el alumno", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void volver(View view) {
        finish();
    }

    public void anadirAlumno(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistroAlumnos.this);
        builder.setTitle("Añadir alumno");
        builder.setMessage("Indique los datos del alumno:");

        Button botonImagen = crearBotonImagen();
        EditText campoNombre = crearCampoNombre();
        EditText campoAñoCurso = crearCampoAñoCurso();
        Spinner spinnerNivelEducativo = crearDesplegableNivelEducativo();
        EditText campoAsignatura = crearCampoAsignatura();
        uriElegidaCliente = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.logo);
        imagenRecortada = new ImageView(RegistroAlumnos.this);
        imagenRecortada.setImageResource(R.drawable.logo);
        imagenRecortada.setLayoutParams(new LinearLayout.LayoutParams(500, 500));
        imagenRecortada.setAdjustViewBounds(true);

        LinearLayout layoutCampos = crearLayoutCampos(imagenRecortada, botonImagen, campoNombre, campoAñoCurso, spinnerNivelEducativo, campoAsignatura);
        builder.setView(layoutCampos);

        botonImagen.setOnClickListener(v -> seleccionarImagen());

        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String nombre = obtenerTexto(campoNombre);
            String anio = obtenerTexto(campoAñoCurso);
            String asignatura = obtenerTexto(campoAsignatura);
            String nivelEducativo = spinnerNivelEducativo.getSelectedItem().toString();
            String curso = anio + "º " + nivelEducativo;

            if (nombre.equals("") && anio.equals("") && asignatura.equals("")) {
                mostrarMensaje("Complete los campos requeridos");
            } else {
                String nuevoID = alumnosRefDB.document().getId();
                String nombreImagen = generarNombreImagen();
                StorageReference imageRef = storageRef.child(nombreImagen);

                subirImagenFirebase(imageRef, nuevoID, nombre, curso);
            }
        });

        builder.show();
    }

    private Button crearBotonImagen() {
        Button botonImagen = new Button(RegistroAlumnos.this);
        botonImagen.setText("Seleccionar imagen");
        return botonImagen;
    }

    private EditText crearCampoNombre() {
        EditText campoNombre = new EditText(RegistroAlumnos.this);
        campoNombre.setHint("Nombre y apellidos");
        return campoNombre;
    }

    private EditText crearCampoAñoCurso() {
        EditText campoAñoCurso = new EditText(RegistroAlumnos.this);
        campoAñoCurso.setHint("Año del curso");
        return campoAñoCurso;
    }

    private Spinner crearDesplegableNivelEducativo() {
        String[] nivelesEducativos = {"ESO", "BACHILLER", "GRADO MEDIO", "GRADO SUPERIOR", "FP MEDIO", "FP SUPERIOR"};
        Spinner spinnerNivelEducativo = new Spinner(RegistroAlumnos.this);
        ArrayAdapter<String> adapterNivelEducativo = new ArrayAdapter<>(RegistroAlumnos.this, android.R.layout.simple_spinner_dropdown_item, nivelesEducativos);
        spinnerNivelEducativo.setAdapter(adapterNivelEducativo);
        return spinnerNivelEducativo;
    }

    private EditText crearCampoAsignatura() {
        EditText campoAsignatura = new EditText(RegistroAlumnos.this);
        campoAsignatura.setHint("Asignatura");
        return campoAsignatura;
    }

    private LinearLayout crearLayoutCampos(ImageView imagen, Button botonImagen, EditText campoNombre, EditText campoAñoCurso, Spinner spinnerNivelEducativo, EditText campoAsignatura) {
        LinearLayout layoutCampos = new LinearLayout(RegistroAlumnos.this);
        layoutCampos.setOrientation(LinearLayout.VERTICAL);
        layoutCampos.setGravity(Gravity.CENTER);
        layoutCampos.setPadding(50, 50, 50, 50);

        layoutCampos.addView(imagen);
        layoutCampos.addView(botonImagen);
        layoutCampos.addView(campoNombre);
        layoutCampos.addView(campoAñoCurso);
        layoutCampos.addView(spinnerNivelEducativo);
        layoutCampos.addView(campoAsignatura);

        return layoutCampos;
    }

    private void seleccionarImagen() {
        Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(i, 1);

    }

    private String obtenerTexto(EditText campo) {
        return campo.getText().toString().replaceAll("\\s+", " ").trim();
    }

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
    }

    private String generarNombreImagen() {
        Date date = new Date();
        String timestamp = String.valueOf(date.getTime());
        return "alumno_" + timestamp + ".jpg";
    }

    private void subirImagenFirebase(StorageReference imageRef, String nuevoID, String nombre, String curso) {
        UploadTask uploadTask = imageRef.putFile(uriElegidaCliente);

        uploadTask.addOnSuccessListener(taskSnapshot -> imageRef.getDownloadUrl().addOnSuccessListener(downloadUrl -> anadirAlumnoDB(downloadUrl, nombre, curso, nuevoID))).addOnFailureListener(e -> mostrarMensaje("Error al subir la imagen"));
    }

    private void anadirAlumnoDB(Uri downloadUrl, String nombre, String curso, String nuevoID) {
        String urlImagen = downloadUrl.toString();
        Map<String, Object> nuevoAlumnoMap = new HashMap<>();
        nuevoAlumnoMap.put("nombre", nombre);
        nuevoAlumnoMap.put("curso", curso);
        nuevoAlumnoMap.put("url_imagen", urlImagen);

        alumnosRefDB.document(nuevoID).set(nuevoAlumnoMap)
                .addOnSuccessListener(aVoid -> anadirAlumnoListaLocal(downloadUrl, nombre, curso, nuevoID))
                .addOnFailureListener(e -> mostrarMensaje("Error al agregar alumno a Firestore"));
    }

    private void anadirAlumnoListaLocal(Uri imagen, String nombre, String curso, String nuevoID) {
        listaAlumnos.add(new Alumno(imagen, nombre, curso, nuevoID));
        adaptadorAlumnos = new AdaptadorAlumnos(RegistroAlumnos.this, listaAlumnos);
        listViewAlumnos.setAdapter(adaptadorAlumnos);
        mostrarMensaje("Alumno agregado con éxito");
    }

    public void volverAtras(View view) {
        onBackPressed();
    }

    @Override
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
            imagenRecortada.setImageDrawable(null);
            uriElegidaCliente = UCrop.getOutput(data);

            imagenRecortada.setImageURI(uriElegidaCliente);

        } else if (resultCode == UCrop.RESULT_ERROR) {
            Throwable error = UCrop.getError(data);
        }
    }
}
