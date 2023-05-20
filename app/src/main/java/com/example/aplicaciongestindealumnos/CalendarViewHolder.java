package com.example.aplicaciongestindealumnos;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class CalendarViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
{
    public final TextView diaMes;
    private final AdaptadorNotaCalendario.OnItemListener onItemListener;
    public CalendarViewHolder(@NonNull View itemView, AdaptadorNotaCalendario.OnItemListener onItemListener)
    {
        super(itemView);
        diaMes = itemView.findViewById(R.id.campoDia);
        this.onItemListener = onItemListener;
        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view)
    {
        onItemListener.onItemClick(getAdapterPosition(), (String) diaMes.getText());
    }
}
