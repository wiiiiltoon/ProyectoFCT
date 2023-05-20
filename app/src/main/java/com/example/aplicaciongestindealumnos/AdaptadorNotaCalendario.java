package com.example.aplicaciongestindealumnos;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

class AdaptadorNotaCalendario extends RecyclerView.Adapter<CalendarViewHolder>
{
    private final ArrayList<String> diasMes;
    private final OnItemListener onItemListener;
    private int selectedItem = -1;

    public AdaptadorNotaCalendario(ArrayList<String> diasMes, OnItemListener onItemListener)
    {
        this.diasMes = diasMes;
        this.onItemListener = onItemListener;
    }

    @NonNull
    @Override
    public CalendarViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.item_calendario, parent, false);
        ViewGroup.LayoutParams layoutParams = view.getLayoutParams();
        layoutParams.height = (int) (parent.getHeight() * 0.166666666);
        return new CalendarViewHolder(view, onItemListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CalendarViewHolder holder, int position) {
        holder.diaMes.setText(diasMes.get(position));

        if (selectedItem == position) {
            holder.diaMes.setTextColor(Color.WHITE);
        } else {
            holder.diaMes.setTextColor(Color.BLACK);
        }

        holder.itemView.setSelected(selectedItem == position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int previousSelectedItem = selectedItem;
                int clickedItem = holder.getAdapterPosition();

                // Ignorar los clics en elementos vacíos
                if (clickedItem >= 0 && clickedItem < diasMes.size() && !diasMes.get(clickedItem).isEmpty()) {
                    selectedItem = (selectedItem == clickedItem) ? -1 : clickedItem;

                    notifyItemChanged(previousSelectedItem);
                    notifyItemChanged(selectedItem);

                    if (onItemListener != null) {
                        if (selectedItem != -1) {
                            onItemListener.onItemClick(selectedItem, diasMes.get(selectedItem));
                        } else {
                            onItemListener.onItemClick(previousSelectedItem, "");
                        }
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount()
    {
        return diasMes.size();
    }

    public interface  OnItemListener
    {
        void onItemClick(int position, String dayText);
    }
}
