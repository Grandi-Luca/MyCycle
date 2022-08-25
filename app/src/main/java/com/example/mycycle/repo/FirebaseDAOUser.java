package com.example.mycycle.repo;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.Objects;

public class FirebaseDAOUser {

    private final DatabaseReference databaseReference;

    public FirebaseDAOUser() {
        this.databaseReference = FirebaseDatabase
                .getInstance("https://auth-89f75-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users");
    }

    public Query getUserInfo() {
        return databaseReference
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
    }
}
