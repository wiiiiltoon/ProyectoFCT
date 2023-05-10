package com.example.aplicaciongestindealumnos;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class SingletonFirebase {
    private static FirebaseFirestore fireBase = null;
    private static FirebaseStorage fireStorage = null;
    private static StorageReference storageReferencia = null;

    private SingletonFirebase() {}


    public static FirebaseFirestore getFireBase() {
        if (fireBase == null) {
            fireBase = FirebaseFirestore.getInstance();
        }
        return fireBase;
    }
    public static FirebaseStorage getStorage() {
        if (fireStorage == null) {
            fireStorage = FirebaseStorage.getInstance();
        }
        return fireStorage;
    }
    public static StorageReference getReferenciaFotos() {
        if (storageReferencia == null) {
            fireStorage = FirebaseStorage.getInstance();
            storageReferencia = fireStorage.getReference("fotoAlumnos/");
        }
        return storageReferencia;
    }
}
