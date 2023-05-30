package com.example.aplicaciongestindealumnos;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

public class GenerarInforme  {
    public static void generarInforme() throws FileNotFoundException {
        String rutaPDF = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString();
        File file = new File(rutaPDF, "AlumnosCalificaciones.pdf");
        OutputStream outputStream = new FileOutputStream(file);

        PdfWriter writer = new PdfWriter(file);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument);

        construirTablaInformacionGeneral(document);
        document.add(new Paragraph("\n"));
        construirTablaCalificaciones(document);

        document.close();
    }

    private static void construirTablaInformacionGeneral(Document document) {
        float[] columnaWidth = {180, 100, 80, 200};
        Table tabla = new Table(columnaWidth);

        // Filas de información general
        agregarCeldaLogo(tabla);
        agregarCeldaVacia(tabla);
        agregarCeldaConTexto(tabla, "Fecha:", "Lunes 14 de marzo 2023");
        agregarCeldaVacia(tabla);
        agregarCeldaConTexto(tabla, "Nombre:", "Wilton Barrueta Anaya");
        agregarCeldaVacia(tabla);
        agregarCeldaConTexto(tabla, "Centro:", "IES El Cañaveral");

        document.add(tabla);
    }

    private static void agregarCeldaLogo(Table tabla) {
        Drawable logo = getDrawable(R.drawable.logo);
        Bitmap bitmap = ((BitmapDrawable) logo).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        byte[] bitmapData = stream.toByteArray();
        ImageData imageData = ImageDataFactory.create(bitmapData);
        Image imagenLogo = new Image(imageData);
        imagenLogo.setHeight(120);

        tabla.addCell(new Cell(6, 1).add(imagenLogo));
    }

    private static void agregarCeldaVacia(Table tabla) {
        tabla.addCell(new Cell().add(new Paragraph("\n")));
    }

    private static void agregarCeldaConTexto(Table tabla, String etiqueta, String valor) {
        tabla.addCell(new Cell().add(new Paragraph("")));
        tabla.addCell(new Cell().add(new Paragraph(etiqueta)));
        tabla.addCell(new Cell().add(new Paragraph(valor)));
    }

    private static void construirTablaCalificaciones(Document document) {
        float[] columnaWidth = {211, 33, 33, 33, 33, 33, 33, 33, 33, 45};
        Table tabla = new Table(columnaWidth);

        // Filas de encabezado de la tabla
        tabla.addCell(new Cell().add(new Paragraph("Nombre del alumno")));
        tabla.addCell(new Cell(1, 8).add(new Paragraph("Calificaciones")));
        tabla.addCell(new Cell().add(new Paragraph("Media")));

        // Filas de calificaciones
        agregarFilaCalificacion(tabla, "Noelia Valladar Aroca", 10, 6, 2, 5, 9, 9, 9, 9, 9);
        agregarFilaCalificacion(tabla, "Wilton Marcos Barrueta Anaya", 5, 9, 9, 9, 9, 10, 6, 2, 9);
        agregarFilaCalificacion(tabla, "Pedro Guido Garcia Tinoco", 10, 2, 9, 9, 5, 6, 9, 9, 9);

        document.add(tabla);
    }

    private static void agregarFilaCalificacion(Table tabla, String nombreAlumno, int... calificaciones) {
        tabla.addCell(new Cell().add(new Paragraph(nombreAlumno)));

        for (int calificacion : calificaciones) {
            tabla.addCell(new Cell().add(new Paragraph(String.valueOf(calificacion))));
        }
    }

    private static Drawable getDrawable(int resourceId) {
        // Implementa la lógica para obtener el recurso de imagen según el resourceId
        return null;
    }
}