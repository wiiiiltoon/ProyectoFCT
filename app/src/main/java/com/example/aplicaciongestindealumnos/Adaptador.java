package com.example.aplicaciongestindealumnos;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

public class Adaptador extends BaseAdapter {

    private Context context;
    private ArrayList<Alumno> listaAlumnos;

    public Adaptador(Context context, ArrayList<Alumno> lista) {
        this.context = context;
        this.listaAlumnos = lista;
    }

    @Override
    public int getCount() {
        return listaAlumnos.size();
    }

    @Override
    public Object getItem(int position) {
        return listaAlumnos.get(position);
    }
    public void remove(Alumno alumno) {
        listaAlumnos.remove(alumno);
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        Alumno item = (Alumno) getItem(position);

        convertView = LayoutInflater.from(context).inflate(R.layout.item_alumno,null);
        ImageView foto = convertView.findViewById(R.id.foto);
        TextView nombre = (TextView) convertView.findViewById(R.id.nombre);
        TextView curso = (TextView) convertView.findViewById(R.id.curso);

        Glide.with(context)
                .load(item.getUrlFoto())
                .into(foto);
        nombre.setText(item.getNombre());
        curso.setText(item.getCurso());
        return convertView;
    }
}
