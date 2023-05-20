package com.example.aplicaciongestindealumnos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdaptadorAsignaturas extends RecyclerView.Adapter<AdaptadorAsignaturas.ViewHolderDatos>{

    ArrayList<String> listaAsignaturas;

    public AdaptadorAsignaturas(ArrayList<String> listaAsignaturas) {
        this.listaAsignaturas = listaAsignaturas;
    }

    @NonNull
    @Override
    public AdaptadorAsignaturas.ViewHolderDatos onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asignatura,null,false);
        return new ViewHolderDatos(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AdaptadorAsignaturas.ViewHolderDatos holder, int position) {
        holder.asignarDatos(listaAsignaturas.get(position));
    }

    @Override
    public int getItemCount() {
        return listaAsignaturas.size();
    }

    public class ViewHolderDatos extends RecyclerView.ViewHolder {

        TextView asignatura;
        public ViewHolderDatos(@NonNull View itemView) {
            super(itemView);
            asignatura = itemView.findViewById(R.id.asignatura);
        }

        public void asignarDatos(String datos) {
            asignatura.setText(datos);
        }
    }
}
