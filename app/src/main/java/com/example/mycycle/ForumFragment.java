package com.example.mycycle;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mycycle.model.QuestionItem;
import com.example.mycycle.model.ReplyItem;
import com.example.mycycle.model.User;
import com.example.mycycle.adapter.QuestionAdapter;
import com.example.mycycle.repo.DAOPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class ForumFragment extends Fragment {

    private QuestionAdapter adapter;
    private DAOPost dao;
    private SwipeRefreshLayout swipeRefreshLayout;
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

        this.adapter = new QuestionAdapter(getActivity());
        this.dao = new DAOPost();

        initWidget(view);

        return view;
    }

    private void initWidget(View view) {

        this.swipeRefreshLayout = view.findViewById(R.id.swipeLayout);

        RecyclerView questionRecyclerView = view.findViewById(R.id.questions);
        questionRecyclerView.setHasFixedSize(true);

        /* Set the layout manager
        and adapter for items
        of the parent recyclerview */
        questionRecyclerView.setAdapter(adapter);
        questionRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        loadData();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                loadData();
                swipeRefreshLayout.setRefreshing(false);
            }
        });


//        TODO: insert new fragment to create a new post
        view.findViewById(R.id.addFab).setOnClickListener(v ->
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
                                        .setQuestionTitle(String.valueOf(++i))
                                        .setQuestionDescription("test")
                                        .setTimestamp(-1 * new Date().getTime())
                                        .setUri(currentUser.getProfilePicture());

                                FirebaseDatabase.getInstance("https://auth-89f75-default-rtdb.europe-west1.firebasedatabase.app/")
                                        .getReference("questions")
                                        .push().setValue(quest)
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

    private void loadData() {
        swipeRefreshLayout.setRefreshing(true);
        this.dao.get(null).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    adapter.clearAll();
                    for (var data : snapshot.getChildren()) {
                        var item = data.getValue(QuestionItem.class);
                        item.setPostID(data.getKey());

                        // get replies
                        dao.getReplies(item.getPostID()).addChildEventListener(new ChildEventListener() {

                            @Override
                            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                                var replies = new ArrayList<ReplyItem>();
                                if (snapshot.exists()) {
                                    replies.add(snapshot.getValue(ReplyItem.class));
                                }
                                item.setQuestionReplies(replies);
                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                            }

                            @Override
                            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        adapter.addQuestion(item);
                        adapter.notifyDataSetChanged();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
                throw error.toException();
            }
        });
    }

}


