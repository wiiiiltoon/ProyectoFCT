package com.example.aplicaciongestindealumnos;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import java.util.ArrayList;

public class ResultadoListasDevueltas implements Parcelable {
    private ArrayList<Alumno> listaObjetos;
    private ArrayList<String> listaStrings;

    public ResultadoListasDevueltas(ArrayList<Alumno> listaObjetos, ArrayList<String> listaStrings) {
        this.listaObjetos = listaObjetos;
        this.listaStrings = listaStrings;
    }

    protected ResultadoListasDevueltas(Parcel in) {
        listaObjetos = in.createTypedArrayList(Alumno.CREATOR);
        listaStrings = in.createStringArrayList();
    }

    public static final Creator<ResultadoListasDevueltas> CREATOR = new Creator<ResultadoListasDevueltas>() {
        @Override
        public ResultadoListasDevueltas createFromParcel(Parcel in) {
            return new ResultadoListasDevueltas(in);
        }

        @Override
        public ResultadoListasDevueltas[] newArray(int size) {
            return new ResultadoListasDevueltas[size];
        }
    };

    public ArrayList<Alumno> getListaObjetos() {
        return listaObjetos;
    }

    public ArrayList<String> getListaStrings() {
        return listaStrings;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeTypedList(listaObjetos);
        dest.writeStringList(listaStrings);
    }
}