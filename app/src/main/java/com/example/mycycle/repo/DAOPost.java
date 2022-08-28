package com.example.mycycle.repo;

import androidx.annotation.NonNull;

import com.example.mycycle.model.ReplyItem;
import com.example.mycycle.model.QuestionItem;
import com.example.mycycle.model.User;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Date;
import java.util.Objects;

public class DAOPost {

    private final FirebaseDatabase databaseReference;

    public DAOPost() {
        databaseReference = FirebaseDatabase
                .getInstance("https://auth-89f75-default-rtdb.europe-west1.firebasedatabase.app");
    }

    public Task<Void> addQuestion(QuestionItem quest) {
        return databaseReference.getReference("questions").push().setValue(quest);
    }

    public Task<Void> addReply(String key, ReplyItem replyItem) {
        return databaseReference.getReference("replies").child(key).push().setValue(replyItem);
    }

    // TODO: add remove/update post

    public Query get() {
        return databaseReference.getReference("questions").orderByChild("timestamp");
    }

    public Query getReplies(String key) {
        return databaseReference.getReference("replies").child(key).orderByChild("timestamp");
    }
}
