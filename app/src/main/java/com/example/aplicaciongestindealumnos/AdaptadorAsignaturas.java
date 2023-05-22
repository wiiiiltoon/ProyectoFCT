package com.example.aplicaciongestindealumnos;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class AdaptadorAsignaturas extends RecyclerView.Adapter<AdaptadorAsignaturas.ViewHolder> {

    private ArrayList<String> listaAsignaturas;

    public AdaptadorAsignaturas(ArrayList<String> listaAsignaturas) {
        this.listaAsignaturas = listaAsignaturas;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_asignatura, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String elemento = listaAsignaturas.get(position);
        holder.textViewItem.setText(elemento);
    }

    @Override
    public int getItemCount() {
        return listaAsignaturas.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView textViewItem;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewItem = itemView.findViewById(R.id.asignatura);
        }
    }
}