package com.example.mycycle;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mycycle.model.QuestionItem;
import com.example.mycycle.model.ReplyItem;
import com.example.mycycle.adapter.QuestionAdapter;
import com.example.mycycle.model.User;
import com.example.mycycle.repo.DAOPost;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ForumFragment extends Fragment {

    private QuestionAdapter adapter;
    private DAOPost dao;

    private SwipeRefreshLayout swipeRefreshLayout;
    private SearchView searchView;

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

        this.adapter = new QuestionAdapter(getActivity(), R.layout.question_item);
        this.dao = new DAOPost();

        initWidget(view);

        return view;
    }

    private void initWidget(View view) {

        this.swipeRefreshLayout = view.findViewById(R.id.swipeLayout);
        this.searchView = view.findViewById(R.id.searchView);

        RecyclerView questionRecyclerView = view.findViewById(R.id.questions);
        questionRecyclerView.setHasFixedSize(true);

        /* Set the layout manager
        and adapter for items
        of the parent recyclerview */
        questionRecyclerView.setAdapter(adapter);
        questionRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        loadData("");

        swipeRefreshLayout.setOnRefreshListener(() -> {
            loadData("");
            searchView.setQuery("", false);
            swipeRefreshLayout.setRefreshing(false);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!query.trim().isEmpty()) {
                    loadData(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.trim().isEmpty()) {
                    loadData("");
                }
                return false;
            }
        });

//      insert new fragment to create a new post
        view.findViewById(R.id.addFab).setOnClickListener(v ->
                showDialog());
    }

    private void loadData(String query) {
        swipeRefreshLayout.setRefreshing(true);
        this.dao.get().addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                swipeRefreshLayout.setRefreshing(true);
                if(snapshot.exists()) {
                    adapter.clearAll();
                    List<QuestionItem> questionItemList = new ArrayList<>();
                    for (var data : snapshot.getChildren()) {
                        var item = data.getValue(QuestionItem.class);
                        Objects.requireNonNull(item).setPostID(data.getKey());
                        questionItemList.add(item);


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
                    }
                    if(!query.trim().isEmpty()) {
                        adapter.setQuestions(questionItemList
                                .stream()
                                .filter(e ->
                                        e.getQuestionTitle().contains(query)
                                                || e.getQuestionDescription().contains(query))
                                .collect(Collectors.toList()));

                    } else {
                        adapter.setQuestions(questionItemList);
                    }
                    adapter.notifyDataSetChanged();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        swipeRefreshLayout.setRefreshing(false);
    }

    private void showDialog() {
        var dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(false);
        dialog.setContentView(R.layout.add_new_post_dialog);
        dialog.getWindow().getAttributes().windowAnimations = R.style.dialog_vertical_swipe_animation;
        dialog.getWindow().setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        var btn_cancel = dialog.findViewById(R.id.closeButton);
        dialog.getWindow().setGravity(Gravity.BOTTOM);
        btn_cancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();

        dialog.findViewById(R.id.addNewPost).setOnClickListener(v -> {
            var title = (EditText) dialog.findViewById(R.id.title);
            var description = (EditText) dialog.findViewById(R.id.questionDescription);

            if(description.getText() == null || description.getText().toString().trim().isEmpty()){
                return;
            }

            dao.addQuestion(
                    title.getText() == null ? "" : title.getText().toString(),
                    description.getText().toString()
            );
            dialog.dismiss();
        });
    }


}


