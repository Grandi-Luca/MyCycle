package com.example.mycycle.repo;

import android.net.Uri;

import com.example.mycycle.CalendarUtils;
import com.example.mycycle.model.NotificationService;
import com.example.mycycle.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Objects;

public class FirebaseDAOUser {

    private final DatabaseReference databaseReference;
    private final StorageReference storageReference;
    private final FirebaseUser currentUser;

    public FirebaseDAOUser() {
        this.currentUser = FirebaseAuth.getInstance().getCurrentUser();

        this.databaseReference = FirebaseDatabase
                .getInstance("https://auth-89f75-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users");

        this.storageReference = FirebaseStorage.getInstance("gs://auth-89f75.appspot.com")
                .getReference().child(Objects.requireNonNull(currentUser).getUid());
    }

    public DatabaseReference getCurrentUserInfo() {
        return databaseReference
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());
    }

    public DatabaseReference getUserInfo(String key) {
        return databaseReference
                .child(key);
    }

    public Task<Void> updateUserInfo(User user) {
        return databaseReference.child(user.getUserID())
                .setValue(user.setUserID(null));
    }

    public UploadTask updateProfileImage(Uri uriProfileImage) {

        // update profile picture on database storage
        return  storageReference.putFile(uriProfileImage);
    }

    public StorageReference getStorageReference() {
        return storageReference;
    }

    public String getCurrentUid() {
        return currentUser.getUid();
    }
}
