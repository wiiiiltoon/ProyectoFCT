package com.example.aplicaciongestindealumnos;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Calendario extends AppCompatActivity implements AdaptadorNotaCalendario.OnItemListener {

    FirebaseFirestore db;
    CollectionReference alumnosRefDB;
    ArrayList<NotaCalendario> listaNotasCalendario;
    private TextView textoMesAño, notaCalendario;
    private RecyclerView calendarRecyclerView;
    private LocalDate fechaSeleccionada;
    String emailDB;
    String diaSeleccionado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendario);

        fechaSeleccionada = LocalDate.now();
        relacionXML();
        recibirIntent();
        inicializarFirebase();
        setMesView();
    }
    private void relacionXML() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        textoMesAño = findViewById(R.id.campoMesAño);
        notaCalendario = findViewById(R.id.notas);
    }
    private void recibirIntent(){
        listaNotasCalendario = new ArrayList<>();
        Intent i = getIntent();
        listaNotasCalendario = i.getParcelableArrayListExtra("listaNotasCalendario");
        emailDB = i.getStringExtra("correoUsuario");
    }
    private void inicializarFirebase(){
        db = SingletonFirebase.getFireBase();
        alumnosRefDB = db.collection("users").document(emailDB).collection("notasCalendario");
    }
    private void setMesView() {
        textoMesAño.setText(formarFechaCalendario(fechaSeleccionada));
        ArrayList<String> diasDelMes = diasMesArray(fechaSeleccionada);
        diasDelMes.add(diasDelMes.remove(0));
        AdaptadorNotaCalendario adaptadorNotaCalendario = new AdaptadorNotaCalendario(diasDelMes, this);
        RecyclerView.LayoutManager layoutManager = new GridLayoutManager(getApplicationContext(), 7);
        calendarRecyclerView.setLayoutManager(layoutManager);
        calendarRecyclerView.setAdapter(adaptadorNotaCalendario);
    }

    private ArrayList<String> diasMesArray(LocalDate date) {
        ArrayList<String> diasMesArray = new ArrayList<>();
        YearMonth añoMes = YearMonth.from(date);
        int ultimoDiaMes = añoMes.lengthOfMonth();
        LocalDate primerDiaMes = fechaSeleccionada.withDayOfMonth(1);
        int diaSemana = primerDiaMes.getDayOfWeek().getValue();
        diaSemana = (diaSemana == 7) ? 1 : diaSemana;

        for (int i = 1; i <= 42; i++) {
            if (i <= diaSemana || i > ultimoDiaMes + diaSemana)
                diasMesArray.add("");
            else
                diasMesArray.add(String.valueOf(i - diaSemana));
        }
        return diasMesArray;
    }

    private String formarFechaCalendario(LocalDate date) {
        DateTimeFormatter formater = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es", "ES"));
        return date.format(formater);
    }

    private String formarFechaBasica(LocalDate date, String dia) {
        DateTimeFormatter formater = DateTimeFormatter.ofPattern("yyyy-MM-" + dia, new Locale("es", "ES"));
        return date.format(formater);
    }

    public void mesAnterior(View view) {
        fechaSeleccionada = fechaSeleccionada.minusMonths(1);
        setMesView();
    }

    public void mesPosterior(View view) {
        fechaSeleccionada = fechaSeleccionada.plusMonths(1);
        setMesView();
    }

    public void anadirNota(View view) {
        if (diaSeleccionado == null) {
            mostrarMensajeToast("Debe seleccionar una fecha");
        } else {
            mostrarDialogoAnadirNota();
        }
    }

    private void mostrarMensajeToast(String mensaje) {
        Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
    }

    private void mostrarDialogoAnadirNota() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Calendario.this);
        builder.setTitle("Añadir nota al día " + diaSeleccionado + " " + formarFechaCalendario(fechaSeleccionada));

        EditText campoEscribirNota = new EditText(Calendario.this);
        LinearLayout layoutCampos = new LinearLayout(Calendario.this);

        layoutCampos.setOrientation(LinearLayout.VERTICAL);
        layoutCampos.setGravity(Gravity.CENTER);
        layoutCampos.setPadding(50, 50, 50, 50);
        layoutCampos.addView(campoEscribirNota);

        builder.setView(layoutCampos);

        builder.setPositiveButton("Añadir", (dialog, which) -> {
            String textoNota = campoEscribirNota.getText().toString().trim();
            notaCalendario.setText(textoNota);

            if (textoNota.equals("")) {
                mostrarMensajeToast("Ingrese una nota");
            } else {
                crearNota(textoNota);
            }
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void crearNota(String textoNota) {
        alumnosRefDB.document(formarFechaBasica(fechaSeleccionada, diaSeleccionado)).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        actualizarNotaExistente(textoNota);
                    } else {
                        crearNuevaNota(textoNota);
                    }
                })
                .addOnFailureListener(e -> mostrarMensajeToast("Error al verificar la existencia del documento"));
    }

    private void actualizarNotaExistente(String textoNota) {
        alumnosRefDB.document(formarFechaBasica(fechaSeleccionada, diaSeleccionado))
                .update("nota", textoNota)
                .addOnSuccessListener(aVoid -> {
                    notaCalendario.setText(textoNota);
                    listaNotasCalendario.add(new NotaCalendario(formarFechaBasica(fechaSeleccionada, diaSeleccionado), textoNota));
                })
                .addOnFailureListener(e -> mostrarMensajeToast("Error al actualizar la nota"));
    }

    private void crearNuevaNota(String textoNota) {
        Map<String, Object> notaMap = new HashMap<>();
        notaMap.put("nota", textoNota);

        alumnosRefDB.document(formarFechaBasica(fechaSeleccionada, diaSeleccionado))
                .set(notaMap, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    notaCalendario.setText(textoNota);
                    listaNotasCalendario.add(new NotaCalendario(formarFechaBasica(fechaSeleccionada, diaSeleccionado), textoNota));
                })
                .addOnFailureListener(e -> mostrarMensajeToast("Error al crear el documento"));
    }

    public void volverAtras(View view) {
        // Acción de volver atrás
        onBackPressed();
    }

    @Override
    public void onItemClick(int position, @NonNull String dia) {
        if (!dia.equals("")) {
            diaSeleccionado = dia;
            String fechaResaltada = formarFechaBasica(fechaSeleccionada, dia);
            boolean notaEncontrada = false;

            for (NotaCalendario objetoNotaCalendario : listaNotasCalendario) {
                if (objetoNotaCalendario.getFecha().equals(fechaResaltada)) {
                    notaCalendario.setText(objetoNotaCalendario.getNota());
                    notaEncontrada = true;
                }
            }

            if (!notaEncontrada) {
                notaCalendario.setText("No hay notas para este día");
            }
        }
    }
}