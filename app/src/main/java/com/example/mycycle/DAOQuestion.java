package com.example.mycycle;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class DAOQuestion {

    private DatabaseReference databaseReference;

    public DAOQuestion() {
        databaseReference = FirebaseDatabase
                .getInstance("https://auth-89f75-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("questions");
    }

    public Task<Void> add(QuestionItem questionItem) {
        return databaseReference.push().setValue(questionItem);
    }

    // TODO: add remove/update post

    public Query get(String key) {

        if(key != null && !key.isEmpty()) {
            return databaseReference.orderByChild("timestamp").startAfter(Long.parseLong(key)).limitToFirst(Utils.MAX_NUMBER_OF_LOAD_CARD);
        }
        return databaseReference.orderByChild("timestamp").limitToFirst(Utils.MAX_NUMBER_OF_LOAD_CARD);
    }
}