package com.example.aplicaciongestindealumnos;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.Date;

public class Calificacion implements Parcelable {

    private Date fecha;
    private String nombreCalificacion;
    private Double calificacion;

    public Calificacion(Date fecha, String nombreCalificacion, Double calificacion) {
        this.fecha = fecha;
        this.nombreCalificacion = nombreCalificacion;
        this.calificacion = calificacion;
    }

    protected Calificacion(Parcel in) {
        nombreCalificacion = in.readString();
        calificacion = in.readDouble();
    }

    public static final Creator<Calificacion> CREATOR = new Creator<Calificacion>() {
        @Override
        public Calificacion createFromParcel(Parcel in) {
            return new Calificacion(in);
        }

        @Override
        public Calificacion[] newArray(int size) {
            return new Calificacion[size];
        }
    };

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getNombreCalificacion() {
        return nombreCalificacion;
    }

    public void setNombreCalificacion(String nombreCalificacion) {
        this.nombreCalificacion = nombreCalificacion;
    }

    public Double getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(Double calificacion) {
        this.calificacion = calificacion;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(nombreCalificacion);
        dest.writeDouble(calificacion);
    }
}
