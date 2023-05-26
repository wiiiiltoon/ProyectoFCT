package com.example.aplicaciongestindealumnos;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.List;

public class AdaptadorCalificaciones extends BaseAdapter {
    private Context context;
    private List<Calificacion> calificaciones;
    DateFormat dateFormat;

    public AdaptadorCalificaciones(Context context, List<Calificacion> calificaciones) {
        this.context = context;
        this.calificaciones = calificaciones;
        dateFormat = android.text.format.DateFormat.getDateFormat(context);
    }

    @Override
    public int getCount() {
        return calificaciones.size();
    }

    @Override
    public Object getItem(int position) {
        return calificaciones.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_calificacion, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textViewFecha = convertView.findViewById(R.id.fecha);
            viewHolder.textViewNombre = convertView.findViewById(R.id.nombreCalificacion);
            viewHolder.textViewCalificacion = convertView.findViewById(R.id.calificacion);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Calificacion calificacion = (Calificacion) getItem(position);

        viewHolder.textViewFecha.setText(dateFormat.format(calificacion.getFecha()));
        viewHolder.textViewNombre.setText(calificacion.getNombreCalificacion());
        viewHolder.textViewCalificacion.setText(String.valueOf(calificacion.getCalificacion()));
        convertView.setBackgroundResource(R.drawable.item_borde_tabla);
        return convertView;
    }

    static class ViewHolder {
        TextView textViewFecha;
        TextView textViewNombre;
        TextView textViewCalificacion;
    }
}