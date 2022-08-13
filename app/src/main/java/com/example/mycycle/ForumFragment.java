package com.example.mycycle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mycycle.recycleView.QuestionAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class ForumFragment extends Fragment {

    private boolean isLoading = false, more = false;
    private QuestionAdapter adapter;
    private String key;
    private User currentUser;
    private static int i = 0;

    public ForumFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_forum, container, false);
        initWidget(view);

        return view;
    }

    private void initWidget(View view) {
        this.adapter = new QuestionAdapter(getContext());

        RecyclerView questionRecyclerView = view.findViewById(R.id.questions);
        questionRecyclerView.setHasFixedSize(true);

        /* Set the layout manager
        and adapter for items
        of the parent recyclerview */
        questionRecyclerView.setAdapter(adapter);
        questionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        loadData();

        questionRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int totalItem = Objects.requireNonNull(linearLayoutManager).getItemCount();
                int lastVisible = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if(totalItem < lastVisible + 3){
                    if(!isLoading) {
                        more = true;
                        isLoading = true;
                        loadData();
                    }
                }
            }
        });


//        TODO: insert new fragment to create a new post
        view.findViewById(R.id.addFab).setOnClickListener(v ->
                FirebaseDatabase.getInstance("https://auth-89f75-default-rtdb.europe-west1.firebasedatabase.app/")
                .getReference("users")
                .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                        .addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                currentUser = snapshot.getValue(User.class);
                                Objects.requireNonNull(currentUser).setUserID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                                var quest = new QuestionItem()
                                        .setUserID(currentUser.getUserID())
                                        .setNickname(Objects.requireNonNull(currentUser).getNickname())
                                        .setQuestionTitle(String.valueOf(++i))
                                        .setQuestionDescription("test")
                                        .setQuestionReplies(new ArrayList<>())
                                        .setTimestamp(-1 * new Date().getTime())
                                        .setUri(currentUser.getProfilePicture());

                                FirebaseDatabase.getInstance("https://auth-89f75-default-rtdb.europe-west1.firebasedatabase.app/")
                                        .getReference("questions")
                                        .push()
                                        .setValue(quest)
                                        .addOnCompleteListener(task -> {
                                            if (task.isSuccessful()){
                                                Toast.makeText(getActivity(), "post has been added successfully", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Toast.makeText(getActivity(), "Failed to submit new post! Try again", Toast.LENGTH_LONG).show();
                                            }
                                        });
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        }));
    }

    private void loadData(){
        Query query;
        if (more){
            query = FirebaseDatabase
                    .getInstance("https://auth-89f75-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("questions")
                    .orderByChild("timestamp").startAfter(Long.parseLong(key)).limitToFirst(8);
        } else {
            query = FirebaseDatabase
                    .getInstance("https://auth-89f75-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("questions")
                    .orderByChild("timestamp").limitToFirst(8);
        }

        query.addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        var quests = new ArrayList<QuestionItem>();
                        for (DataSnapshot data : snapshot.getChildren()) {
                            QuestionItem item = data.getValue(QuestionItem.class);
                            quests.add(item);
                            key = String.valueOf(Objects.requireNonNull(item).getTimestamp());

                        }
                        if (more) {
                            adapter.addAllQuests(quests);
                        } else {
                            adapter.setQuestions(quests);
                        }
                        adapter.notifyDataSetChanged();

                        isLoading = false;

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
    }

}


