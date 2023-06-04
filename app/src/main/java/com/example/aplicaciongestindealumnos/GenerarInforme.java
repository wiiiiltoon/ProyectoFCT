package com.example.aplicaciongestindealumnos;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.widget.Toast;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.HorizontalAlignment;
import com.itextpdf.layout.property.TextAlignment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class GenerarInforme  {
    Context contexto;
    String nombre, centro, asignatura;
    int numNotas;
    CollectionReference alumnosCollection;
    Drawable logo;
    ArrayList<Alumno> listaAlumnos;

    public GenerarInforme(Context contexto, String nombre, String centro, String asignatura, CollectionReference alumnosCollection, Drawable logo, ArrayList<Alumno> listaAlumnos, int numNotas) throws FileNotFoundException {
        this.contexto = contexto;
        this.nombre = nombre;
        this.centro = centro;
        this.asignatura = asignatura;
        this.alumnosCollection = alumnosCollection;
        this.logo = logo;
        this.listaAlumnos = listaAlumnos;
        this.numNotas = numNotas;
        generarInforme();
    }

    public void generarInforme() throws FileNotFoundException {
        String rutaPDF = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File file = new File(rutaPDF, "AlumnosCalificaciones.pdf");
        OutputStream outputStream = new FileOutputStream(file);

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        float columnaWidth[] = {20, 200, 80, 200};
        Table tabla = new Table(columnaWidth);
        tabla.setHorizontalAlignment(HorizontalAlignment.CENTER);


        Date date = new Date();
        SimpleDateFormat formato = new SimpleDateFormat("EEEE d 'de' MMMM yyyy", new Locale("es"));
        String fecha = formato.format(date);

        //Logo
        agregarCeldaVacia(tabla, 4);
        agregarLogo(tabla);
        // Fila 1
        agregarCeldaConTexto(tabla, "Fecha:", true);
        agregarCeldaConTexto(tabla, fecha, false);
        //Fila 2
        agregarCeldaVacia(tabla, 2);
        //Fila 3
        agregarCeldaConTexto(tabla, "Profesor:", true);
        agregarCeldaConTexto(tabla, nombre, false);
        //Fila 4
        agregarCeldaConTexto(tabla, "Centro:", true);
        agregarCeldaConTexto(tabla, centro, false);
        //Fila 5
        agregarCeldaConTexto(tabla, "Asignatura:", true);
        agregarCeldaConTexto(tabla, asignatura, false);
        //SeparacionTablas
        agregarCeldaVacia(tabla, 8);

        ArrayList<Alumno> alumnosFiltrados = new ArrayList<>();
        for (Alumno alumno : listaAlumnos) {
            ArrayList<String> listAsig = alumno.getAsignaturas();
            if (!listAsig.isEmpty()) {
                for (String asig : listAsig) {
                    if (asig.equals(asignatura)) {
                        alumnosFiltrados.add(alumno);
                    }
                }
            }
        }
        Table tabla2 = new Table(2 + numNotas);
        tabla2.setHorizontalAlignment(HorizontalAlignment.CENTER);
        Color color = new DeviceRgb(183, 213, 229);
        tabla2.addCell(new Cell().add(new Paragraph("Nombre del alumno").setBackgroundColor(color).setBold().setPadding(3)));
        for (int i = 0; i < numNotas; i++) {
            tabla2.addCell(new Cell().add(new Paragraph("Nota " + (i + 1)).setBackgroundColor(color).setBold().setPadding(3)));
        }
        tabla2.addCell(new Cell().add(new Paragraph("Media").setBackgroundColor(color).setBold().setPadding(3)));

        crearTablaCalificaciones(document, asignatura, alumnosFiltrados, tabla, tabla2);
    }

    private void agregarCeldaVacia(Table tabla, int numCeldas) {
        for (int i = 0; i < numCeldas; i++) {
            tabla.addCell(new Cell().add(new Paragraph("\n")).setBorder(Border.NO_BORDER));
        }
    }

    private void agregarLogo(Table tabla) {
        Bitmap bitmap = ((BitmapDrawable) logo).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapData = stream.toByteArray();
        ImageData imageData = ImageDataFactory.create(bitmapData);
        Image imagenLogo = new Image(imageData);
        imagenLogo.setHeight(110);

        tabla.addCell(new Cell(5, 1).add(new Paragraph("")).setBorder(Border.NO_BORDER));
        tabla.addCell(new Cell(5, 1).add(imagenLogo).setBorder(Border.NO_BORDER));
    }

    private void agregarCeldaConTexto(Table tabla, String texto, boolean bold) {
        Cell celda = new Cell().add(new Paragraph(texto));
        if (bold) {
            celda.setBold();
        }
        celda.setBorder(Border.NO_BORDER);
        tabla.addCell(celda);
    }

    private void crearTablaCalificaciones(Document document, String asignatura, ArrayList<Alumno> alumnos, Table tabla, Table tabla2) {
        ArrayList<String> nombresAlumnos = new ArrayList<>();
        ArrayList<ArrayList<Double>> notasAlumnos = new ArrayList<>();
        ArrayList<Double> mediasAlumnos = new ArrayList<>();

        if(!alumnos.isEmpty()){
            for (Alumno alumno : alumnos) {
                alumnosCollection.document(alumno.getIdFireBase()).collection("asignaturas").document(asignatura)
                        .collection("calificaciones")
                        .orderBy("fecha", Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener(queryDocumentSnapshots -> {
                            ArrayList<Double> notas = new ArrayList<>();
                            for (QueryDocumentSnapshot document1 : queryDocumentSnapshots) {
                                Double nota = document1.getDouble("nota");
                                if (nota != null) {
                                    notas.add(nota);
                                }
                            }
                            if (notas.size() > numNotas) {
                                notas.subList(numNotas, notas.size()).clear();
                            }
                            while (notas.size() < numNotas) {
                                notas.add(null);
                            }
                            double media = calcularMedia(notas);
                            nombresAlumnos.add(alumno.getNombre());
                            notasAlumnos.add(notas);
                            mediasAlumnos.add(media);
                            if (nombresAlumnos.size() == alumnos.size()) {
                                for (int i = 0; i < nombresAlumnos.size(); i++) {
                                    tabla2.addCell(new Cell().add(new Paragraph(nombresAlumnos.get(i))).setPadding(3));
                                    for (Double nota : notasAlumnos.get(i)) {
                                        String notaTabla = nota != null ? String.valueOf(nota) : "";
                                        añadirFilaTablaCalificaciones(tabla2, notaTabla);
                                    }
                                    añadirFilaTablaCalificaciones(tabla2, String.valueOf(mediasAlumnos.get(i)));
                                }
                                document.add(tabla);
                                document.add(tabla2);
                                document.add(new Paragraph("\n"));
                                document.close();
                                mostrarMensaje("Pdf creado.");
                            }
                        });
            }
        }else{
            mostrarMensaje("No hay alumnos para la asignatura de "+asignatura);
        }

    }

    private void añadirFilaTablaCalificaciones(Table tabla2, String datosTabla) {
        tabla2.addCell(new Cell().add(new Paragraph(datosTabla)).setPadding(3).setTextAlignment(TextAlignment.CENTER));
    }

    private double calcularMedia(ArrayList<Double> notas) {
        double suma = 0;
        int count = 0;

        for (Double nota : notas) {
            if (nota != null) {
                suma += nota;
                count++;
            }
        }
        if (count > 0) {
            double media = suma / count;
            return Math.round(media * 100.0) / 100.0;
        } else {
            return 0;
        }
    }
    private void mostrarMensaje(String mensaje) {
        Toast.makeText(contexto, mensaje, Toast.LENGTH_SHORT).show();
    }
}