package com.example.mycycle.repo;

import com.example.mycycle.model.ReplyItem;
import com.example.mycycle.model.QuestionItem;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

public class DAOPost {

    private FirebaseDatabase databaseReference;

    public DAOPost() {
        databaseReference = FirebaseDatabase
                .getInstance("https://auth-89f75-default-rtdb.europe-west1.firebasedatabase.app");
    }

    public Task<Void> addQuestion(QuestionItem questionItem) {
        return databaseReference.getReference("questions").push().setValue(questionItem);
    }

    public Task<Void> addReply(String key, ReplyItem replyItem) {
        return databaseReference.getReference("replies").child(key).push().setValue(replyItem);
    }

    // TODO: add remove/update post

    public Query get(String key) {
        return databaseReference.getReference("questions").orderByChild("timestamp");
    }

    public Query getReplies(String key) {
        return databaseReference.getReference("replies").child(key).orderByChild("timestamp");
    }
}
