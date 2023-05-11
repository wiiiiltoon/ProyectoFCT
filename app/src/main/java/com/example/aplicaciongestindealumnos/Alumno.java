package com.example.aplicaciongestindealumnos;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.ImageView;

import androidx.annotation.NonNull;

public class Alumno implements Parcelable {

    private Uri urlFoto;
    private String nombre;
    private String curso;
    private String idFirebase;

    public Alumno(Uri urlFoto,String nombre, String curso,String idFirebase){
        this.urlFoto = urlFoto;
        this.nombre = nombre;
        this.curso = curso;
        this.idFirebase = idFirebase;
    }

    protected Alumno(Parcel in) {
        urlFoto = in.readParcelable(Uri.class.getClassLoader());
        nombre = in.readString();
        curso = in.readString();
        idFirebase = in.readString();
    }

    public static final Creator<Alumno> CREATOR = new Creator<Alumno>() {
        @Override
        public Alumno createFromParcel(Parcel in) {
            return new Alumno(in);
        }

        @Override
        public Alumno[] newArray(int size) {
            return new Alumno[size];
        }
    };

    public Uri getUrlFoto() {
        return urlFoto;
    }
    public String getIdFireBase() {
        return idFirebase;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCurso() {
        return curso;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeParcelable(urlFoto, flags);
        dest.writeString(nombre);
        dest.writeString(curso);
        dest.writeString(idFirebase);
    }
}
