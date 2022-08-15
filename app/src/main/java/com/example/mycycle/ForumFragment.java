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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mycycle.recycleView.QuestionAdapter;
import com.example.mycycle.recycleView.QuestionItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

public class ForumFragment extends Fragment {

    private boolean isLoading;
    private QuestionAdapter adapter;
    private String key;
    private DAOPost dao;
    private User currentUser;
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
        this.dao = new DAOPost("questions");
        this.key = null;
        this.isLoading = false;

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

        loadData(false);

        questionRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                LinearLayoutManager linearLayoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int visibleItemCount = Objects.requireNonNull(linearLayoutManager).getChildCount();
                int totalItemCount = Objects.requireNonNull(linearLayoutManager).getItemCount();
                int lastVisible = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                if(lastVisible + visibleItemCount >= totalItemCount) {
                    if(!isLoading) {
                        isLoading = true;
                        loadData(true);
                    }
                }
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
                                currentUser = snapshot.getValue(User.class);
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
                                                System.err.println(key);
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

    private void loadData(boolean onScroll) {
        swipeRefreshLayout.setRefreshing(true);
        this.dao.get(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!onScroll) {
                    adapter.clearAll();
                }
                final var questsList = new ArrayList<QuestionItem>();
                for (var data : snapshot.getChildren()){
                    QuestionItem quest = data.getValue(QuestionItem.class);
                    Objects.requireNonNull(quest).setPostID(data.getKey());
                    questsList.add(quest);
                    key = String.valueOf(Objects.requireNonNull(quest).getTimestamp());
                }
                adapter.setQuestions(questsList);
                adapter.notifyDataSetChanged();

                isLoading = false;
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
                throw error.toException();
            }
        });
    }

}


