package com.example.aplicaciongestindealumnos;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class NotaCalendario implements Parcelable {

    private String nota;
    private String fecha;

    public NotaCalendario(String fecha, String nota){
        this.fecha = fecha;
        this.nota = nota;
    }

    protected NotaCalendario(Parcel in) {
        fecha = in.readString();
        nota = in.readString();
    }

    public static final Creator<NotaCalendario> CREATOR = new Creator<NotaCalendario>() {
        @Override
        public NotaCalendario createFromParcel(Parcel in) {
            return new NotaCalendario(in);
        }

        @Override
        public NotaCalendario[] newArray(int size) {
            return new NotaCalendario[size];
        }
    };

    public String getFecha() {
        return fecha;
    }

    public String getNota() {
        return nota;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(fecha);
        dest.writeString(nota);
    }
}
