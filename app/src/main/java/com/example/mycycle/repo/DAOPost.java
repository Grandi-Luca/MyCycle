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

    public void addQuestion(String title, String description) {
        FirebaseDatabase.getInstance("https://auth-89f75-default-rtdb.europe-west1.firebasedatabase.app/")
            .getReference("users")
            .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        var currentUser = snapshot.getValue(User.class);
                        Objects.requireNonNull(currentUser).setUserID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                        var quest = new QuestionItem()
                                .setUserID(currentUser.getUserID())
                                .setNickname(Objects.requireNonNull(currentUser).getNickname())
                                .setQuestionTitle(String.valueOf(title))
                                .setQuestionDescription(description)
                                .setTimestamp(-1 * new Date().getTime())
                                .setUri(currentUser.getProfilePicture());

                        databaseReference.getReference("questions").push().setValue(quest);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
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
