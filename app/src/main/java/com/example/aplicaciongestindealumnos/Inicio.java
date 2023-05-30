package com.example.aplicaciongestindealumnos;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.colors.WebColors;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;

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
        setContentView(R.layout.inicio);

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
        Intent i = new Intent(Inicio.this, Eleccion_consultas.class);
        i.putExtra("correoUsuario", emailDB);
        i.putExtra("listaAlumnos", gestorAlumnos.obtenerTodosAlumnos());
        i.putExtra("listaAsignaturas", gestorAsignaturas.obtenerTodasAsignaturas());
        startActivity(i);
    }
    public void intentEleccionRegistros(View view){
        Intent i = new Intent(Inicio.this, Eleccion_registros.class);
        i.putExtra("correoUsuario", emailDB);
        i.putExtra("listaAlumnos", gestorAlumnos.obtenerTodosAlumnos());
        i.putExtra("listaAsignaturas", gestorAsignaturas.obtenerTodasAsignaturas());
        launcher.launch(i);
    }
    public void intentCalendario(View view) {
        Intent i = new Intent(Inicio.this, Calendario.class);
        i.putExtra("listaNotasCalendario", listaNotasCalendario);
        i.putExtra("correoUsuario", emailDB);
        startActivity(i);
    }
    public void generarInforme(View view) throws FileNotFoundException {
        String rutaPDF = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File file = new File(rutaPDF, "AlumnosCalificaciones.pdf");
        OutputStream outputStream = new FileOutputStream(file);

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        float columnaWidth[] = {20,200,60,200,80};
        Table tabla = new Table(columnaWidth);

        //fila 1
        Drawable logo= getDrawable(R.drawable.logo);
        Bitmap bitmap = ((BitmapDrawable)logo).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
        byte[] bitmapData = stream.toByteArray();
        ImageData imageData = ImageDataFactory.create(bitmapData);
        Image imagenLogo = new Image(imageData);
        imagenLogo.setHeight(110);

        tabla.addCell(new Cell(5,1).add(new Paragraph("")).setBorder(Border.NO_BORDER));
        tabla.addCell(new Cell(5,1).add(imagenLogo).setBorder(Border.NO_BORDER));
        tabla.addCell(new Cell().add(new Paragraph("Fecha:").setBold()).setBorder(Border.NO_BORDER));
        tabla.addCell(new Cell().add(new Paragraph("Lunes 14 de marzo 2023")).setBorder(Border.NO_BORDER));
        tabla.addCell(new Cell().add(new Paragraph("")).setBold().setBorder(Border.NO_BORDER));
        //fila 2
        //tabla.addCell(new Cell().add(new Paragraph("")));
        tabla.addCell(new Cell().add(new Paragraph("\n")).setBorder(Border.NO_BORDER));
        tabla.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        tabla.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        //fila 3
        //tabla.addCell(new Cell().add(new Paragraph("")));
        tabla.addCell(new Cell().add(new Paragraph("\n")).setBorder(Border.NO_BORDER));
        tabla.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        tabla.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        //fila 4
        //tabla.addCell(new Cell().add(new Paragraph("")));
        tabla.addCell(new Cell().add(new Paragraph("Profesor:").setBold()).setBorder(Border.NO_BORDER));
        tabla.addCell(new Cell().add(new Paragraph("Wilton Barrueta Anaya")).setBorder(Border.NO_BORDER));
        tabla.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));
        //fila 5
        //tabla.addCell(new Cell().add(new Paragraph("")));
        tabla.addCell(new Cell().add(new Paragraph("Centro:").setBold()).setBorder(Border.NO_BORDER));
        tabla.addCell(new Cell().add(new Paragraph("IES El Cañaveral")).setBorder(Border.NO_BORDER));
        tabla.addCell(new Cell().add(new Paragraph("")).setBorder(Border.NO_BORDER));

        float columnaWidth2[] = {212,33,33,33,33,33,33,33,33,45};
        Table tabla2 = new Table(columnaWidth2);
        //fila 0
        Color color2 = WebColors.getRGBColor("#B7D5E5");
        Color color = new DeviceRgb(183, 213, 229);
        tabla2.addCell(new Cell().add(new Paragraph("Nombre del alumno").setBackgroundColor(color).setBold()));
        tabla2.addCell(new Cell(1,8).add(new Paragraph("Calificaciones").setBackgroundColor(color).setBold()));
        tabla2.addCell(new Cell().add(new Paragraph("Media").setBackgroundColor(color).setBold()));
        //fila 1
        ArrayList<Integer> numeros = new ArrayList<>(Arrays.asList(9,1,3,6,2,8,5,7));
        tabla2.addCell(new Cell().add(new Paragraph("Wilton Marcos Barrueta Anaya")));

        float sum = 0;
        for (int i = 0; i < numeros.size(); i++) {
            int numero = numeros.get(i);
            tabla2.addCell(new Cell().add(new Paragraph(String.valueOf(numero))));
            sum += numero;
        }
        float media = sum / numeros.size();
        tabla2.addCell(new Cell().add(new Paragraph(String.valueOf(media)).setBold()));

        //fila 2
        ArrayList<Integer> numeros2 = new ArrayList<>(Arrays.asList(3,7,5,2,7,5,8,5));
        tabla2.addCell(new Cell().add(new Paragraph("Wilton Marcos Barrueta Anaya")));

        float sum2 = 0;
        for (int i = 0; i < numeros2.size(); i++) {
            int numero = numeros2.get(i);
            tabla2.addCell(new Cell().add(new Paragraph(String.valueOf(numero))));
            sum2 += numero;
        }
        float media2 = sum2 / numeros2.size();
        tabla2.addCell(new Cell().add(new Paragraph(String.valueOf(media2)).setBold()));

        //fila 3
        ArrayList<Integer> numeros3 = new ArrayList<>(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8));
        tabla2.addCell(new Cell().add(new Paragraph("Pedro Guido Garcia Tinoco")));

        float sum3 = 0;
        for (int i = 0; i < numeros3.size(); i++) {
            int numero = numeros3.get(i);
            tabla2.addCell(new Cell().add(new Paragraph(String.valueOf(numero))));
            sum3 += numero;
        }
        float media3 = sum3 / numeros3.size();
        tabla2.addCell(new Cell().add(new Paragraph(String.valueOf(media3)).setBold()));

        document.add(tabla);
        document.add(new Paragraph("\n"));
        document.add(tabla2);
        document.close();
        mostrarMensaje("Pdf creado.");
    }

    private ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Intent data = result.getData();
                    ResultadoListasDevueltas resultado = data.getParcelableExtra("listasDevueltas");
                    gestorAlumnos.setListaAlumnos(resultado.getListaObjetos());
                    gestorAsignaturas.setListaAsignaturas(resultado.getListaStrings());
                }
            }
    );

    private void mostrarMensaje(String mensaje) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show();
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
