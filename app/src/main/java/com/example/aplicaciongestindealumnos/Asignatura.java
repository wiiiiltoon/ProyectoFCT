package com.example.aplicaciongestindealumnos;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class Asignatura implements Parcelable {

    String idAsignatura;

    public Asignatura(String idAsignatura) {
        this.idAsignatura = idAsignatura;
    }

    public String getIdAsignatura() {
        return idAsignatura;
    }

    public void setIdAsignatura(String idAsignatura) {
        this.idAsignatura = idAsignatura;
    }

    protected Asignatura(Parcel in) {
        idAsignatura = in.readString();
    }

    public static final Creator<Asignatura> CREATOR = new Creator<Asignatura>() {
        @Override
        public Asignatura createFromParcel(Parcel in) {
            return new Asignatura(in);
        }

        @Override
        public Asignatura[] newArray(int size) {
            return new Asignatura[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(idAsignatura);
    }
}
