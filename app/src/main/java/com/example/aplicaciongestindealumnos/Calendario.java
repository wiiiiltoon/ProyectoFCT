package com.example.aplicaciongestindealumnos;

import android.app.AlertDialog;
import android.content.DialogInterface;
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

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
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

        listaNotasCalendario = new ArrayList<>();
        db = SingletonFirebase.getFireBase();
        Intent i = getIntent();
        listaNotasCalendario = i.getParcelableArrayListExtra("listaNotasCalendario");
        emailDB = i.getStringExtra("correoUsuario");
        alumnosRefDB = db.collection("users").document(emailDB).collection("notasCalendario");
        initWidgets();
        fechaSeleccionada = LocalDate.now();
        notaCalendario = findViewById(R.id.notas);
        setMesView();
    }

    private void initWidgets() {
        calendarRecyclerView = findViewById(R.id.calendarRecyclerView);
        textoMesAño = findViewById(R.id.campoMesAño);
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
            Toast.makeText(getApplicationContext(), "Debe seleccionar una fecha", Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(Calendario.this);
            builder.setTitle("Añadir nota al dia " + diaSeleccionado + " " + formarFechaCalendario(fechaSeleccionada));

            // Crear el campo de texto multilinea
            EditText campoEscribirNota = new EditText(Calendario.this);
            campoEscribirNota.setMinLines(1);
            campoEscribirNota.setMaxLines(5);

            LinearLayout layoutCampos = new LinearLayout(Calendario.this);
            layoutCampos.setOrientation(LinearLayout.VERTICAL);
            layoutCampos.setGravity(Gravity.CENTER);
            layoutCampos.setPadding(50, 50, 50, 50);

            layoutCampos.addView(campoEscribirNota);

            builder.setView(layoutCampos);

            // Agregar botón "Añadir"
            builder.setPositiveButton("Añadir", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String textoNota = campoEscribirNota.getText().toString().trim();
                    notaCalendario.setText(textoNota);

                    if (textoNota.equals("")) {
                        Toast.makeText(getApplicationContext(), "Ingrese un nota", Toast.LENGTH_SHORT).show();
                    } else {
                        alumnosRefDB.document(formarFechaBasica(fechaSeleccionada, diaSeleccionado)).get()
                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                    @Override
                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                        if (documentSnapshot.exists()) {
                                            // El documento ya existe, actualizar la nota
                                            alumnosRefDB.document(formarFechaBasica(fechaSeleccionada, diaSeleccionado))
                                                    .update("nota", textoNota)
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            notaCalendario.setText(textoNota);
                                                            listaNotasCalendario.add(new NotaCalendario(formarFechaBasica(fechaSeleccionada, diaSeleccionado), textoNota));
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(Calendario.this, "Error al actualizar la nota", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        } else {
                                            // El documento no existe, crear un nuevo documento con la nueva nota
                                            Map<String, Object> notaMap = new HashMap<>();
                                            notaMap.put("nota", textoNota);

                                            alumnosRefDB.document(formarFechaBasica(fechaSeleccionada, diaSeleccionado))
                                                    .set(notaMap, SetOptions.merge())
                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                        @Override
                                                        public void onSuccess(Void aVoid) {
                                                            notaCalendario.setText(textoNota);
                                                            listaNotasCalendario.add(new NotaCalendario(formarFechaBasica(fechaSeleccionada, diaSeleccionado), textoNota));
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Toast.makeText(Calendario.this, "Error al crear el documento", Toast.LENGTH_SHORT).show();
                                                        }
                                                    });
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(Calendario.this, "Error al verificar la existencia del documento", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }
            });

            // Agregar botón "Cancelar"
            builder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            AlertDialog dialog = builder.create();
            dialog.show();
        }
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