package com.example.aplicaciongestindealumnos;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
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

public class Inicio extends AppCompatActivity {
    GestorAlumnos gestorAlumnos;
    GestorAsignaturas gestorAsignaturas;
    FirebaseFirestore db;
    FirebaseAuth mAuth;
    FirebaseStorage storage;
    DocumentReference referenciaCorreo;
    String emailDB;
    TextView datoNombre, datoCentro;
    Button consulta, registro, calendario, informe, cerrarSesion, asignaturas;
    ArrayList<NotaCalendario> listaNotasCalendario;
    ArrayList<String> listaAsignaturaAlumno;
    CollectionReference alumnosRefDB, asignaturasRefDB, notasCalendarioRefDB;
    ImageView imagenRecortada;
    Uri uriElegidaCliente;
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

        alumnosRefDB = db.collection("users").document(emailDB).collection("alumnos");
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
        listaNotasCalendario = new ArrayList<>();
    }

    private void inicializarFirebase() {
        db = SingletonFirebase.getFireBase();
        mAuth = FirebaseAuth.getInstance();
        storage = SingletonFirebase.getStorage();
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
            Log.w("Inicio", "El usuario actual es nulo");
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
                Log.e("Clase: Inicio.java", "Error al obtener los alumnos desde la base de datos.", task.getException());
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
                    mostrarMensaje(nombreAsignatura);
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
                Log.e("Clase: Inicio.java", "Error al escuchar los cambios en la base de datos.", error);
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

    public void RegistroAlumnos(View view) {
        mostrarDialog();
    }
    private void mostrarDialog(){
        LayoutInflater inflater = LayoutInflater.from(this);
        View view = inflater.inflate(R.layout.pantalla_registrar_alumno, null);

        imagenRecortada = view.findViewById(R.id.imagenRecortada);
        Button botonImagen = view.findViewById(R.id.botonImagen);
        EditText campoNombre = view.findViewById(R.id.campoNombre);
        EditText campoAñoCurso = view.findViewById(R.id.campoAñoCurso);
        Spinner spinnerNivelEducativo = view.findViewById(R.id.spinnerNivelEducativo);
        GridLayout contenedorAsignaturas = view.findViewById(R.id.contenedorAsignaturas);

        anadirAlumno(view,botonImagen,campoNombre,campoAñoCurso,spinnerNivelEducativo,contenedorAsignaturas);
    }
    public void anadirAlumno(View view, Button botonImagen,EditText campoNombre,EditText campoAñoCurso,Spinner spinnerNivelEducativo,GridLayout contenedorAsignaturas) {
        listaAsignaturaAlumno = new ArrayList<>();
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Añadir alumno");
        builder.setMessage("Indique los datos del alumno:");

        builder.setView(view);

        String[] nivelesEducativos = {"ESO", "BACHILLER", "GRADO MEDIO", "GRADO SUPERIOR", "FP MEDIO", "FP SUPERIOR"};
        ArrayAdapter<String> adapterNivelEducativo = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, nivelesEducativos);
        spinnerNivelEducativo.setAdapter(adapterNivelEducativo);

        if (gestorAsignaturas.obtenerTodasAsignaturas().size() == 0) {
            mostrarMensaje("Lista vacia mi rei");
        } else {
            for (String nombreAsignatura : gestorAsignaturas.obtenerTodasAsignaturas()) {
                CheckBox checkBox = new CheckBox(this);
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

        uriElegidaCliente = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.logo);

        botonImagen.setOnClickListener(v -> seleccionarImagen());

        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String nombre = obtenerTexto(campoNombre);
            String anio = obtenerTexto(campoAñoCurso);
            String nivelEducativo = spinnerNivelEducativo.getSelectedItem().toString();
            String curso = anio + "º " + nivelEducativo;

            if (nombre.equals("") && anio.equals("")) {
                mostrarMensaje("Complete los campos requeridos");
            } else {
                String nuevoID = alumnosRefDB.document().getId();
                String nombreImagen = generarNombreImagen();
                StorageReference imageRef = storageRef.child(nombreImagen);
                subirImagenFirebase(imageRef, nuevoID, nombre, curso, listaAsignaturaAlumno);
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

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
        startActivityForResult(i, 1);

    }
    private void subirImagenFirebase(StorageReference imageRef, String nuevoID, String nombre, String curso,ArrayList<String> asignaturasAlumno) {
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
                        alumnosRefDB.document(nuevoID).collection("asignaturas").add(createAsignaturaData(asignatura))
                                .addOnSuccessListener(documentReference -> {
                                })
                                .addOnFailureListener(e -> mostrarMensaje("Error al agregar asignatura a Firestore"));
                    }
                    gestorAlumnos.añadirAlumno(new Alumno(downloadUrl, nombre, curso, nuevoID,asignaturasAlumno));
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

    public void intentConsultarAlumnos(View view) {
        Intent i = new Intent(Inicio.this, ConsultarAlumnos.class);
        i.putExtra("correoUsuario", emailDB);
        i.putExtra("listaAlumnos", gestorAlumnos.obtenerTodosAlumnos());
        startActivity(i);
    }

    public void registrarAsignaturas(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Añadir asignatura");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layoutView = inflater.inflate(R.layout.item_asignatura, null);
        builder.setView(layoutView);

        EditText editTextNombre = layoutView.findViewById(R.id.editTextNombre);

        builder.setPositiveButton("Aceptar", (dialog, which) -> {
            String nombreAsignatura = editTextNombre.getText().toString().trim();
            if (!nombreAsignatura.isEmpty()) {
                verificarExistenciaAsignatura(nombreAsignatura);
            } else {
                mostrarMensaje("Ingrese un nombre de asignatura válido");
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    private void verificarExistenciaAsignatura(String nombreAsignatura) {
        asignaturasRefDB.document(nombreAsignatura).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        mostrarMensaje("La asignatura ya existe");
                    } else {
                        crearNuevaAsignatura(nombreAsignatura);
                    }
                })
                .addOnFailureListener(e -> mostrarMensaje("Error al verificar la existencia de la asignatura"));
    }

    private void crearNuevaAsignatura(String nombreAsignatura) {
        asignaturasRefDB.document(nombreAsignatura)
                .set(new HashMap<>(), SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    gestorAsignaturas.añadirAsignatura(nombreAsignatura);
                    mostrarMensaje("Asignatura agregada: " + nombreAsignatura);
                })
                .addOnFailureListener(e -> mostrarMensaje("Error al crear la asignatura"));
    }

    public void consultarAsignaturas(View view){
        Intent i = new Intent(Inicio.this, RegistroAsignaturas.class);
        i.putExtra("listaAsignaturas", gestorAsignaturas.obtenerTodasAsignaturas());
        i.putExtra("correoUsuario", emailDB);
        startActivity(i);
    }

    public void intentCalendario(View view) {
        Intent i = new Intent(Inicio.this, Calendario.class);
        i.putExtra("listaNotasCalendario", listaNotasCalendario);
        i.putExtra("correoUsuario", emailDB);
        startActivity(i);
    }

    public void intentRegistroCalificacion(View view) {

        new RegistroCalificacion(this, gestorAlumnos.obtenerTodosAlumnos());
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

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();

            UCrop.Options options = new UCrop.Options();
            options.setCompressionQuality(70);
            options.setToolbarTitle("Recortar imagen");
            options.setStatusBarColor(getResources().getColor(R.color.titulo));
            options.setToolbarColor(getResources().getColor(R.color.azulFondo));
            options.setActiveControlsWidgetColor(getResources().getColor(R.color.blanco));

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
