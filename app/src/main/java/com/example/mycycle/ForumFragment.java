package com.example.mycycle;

import static com.example.mycycle.ProfileFragment.currentUser;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.mycycle.adapter.ReplyAdapter;
import com.example.mycycle.model.QuestionItem;
import com.example.mycycle.adapter.QuestionAdapter;
import com.example.mycycle.model.ReplyItem;
import com.example.mycycle.model.User;
import com.example.mycycle.repo.DAOPost;
import com.example.mycycle.repo.FirebaseDAOUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class ForumFragment extends Fragment implements QuestionAdapter.OnItemListener {

    private QuestionAdapter questionAdapter;
    private ReplyAdapter replyAdapter;
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

        this.questionAdapter = new QuestionAdapter(getActivity(), this);
        this.replyAdapter = new ReplyAdapter(getActivity());
        this.dao = new DAOPost();

        initWidget(view);

        return view;
    }

    private void initWidget(View view) {

        this.swipeRefreshLayout = view.findViewById(R.id.swipeLayout);
        this.searchView = view.findViewById(R.id.searchView);

        RecyclerView questionRecyclerView = view.findViewById(R.id.recyclerList);
        questionRecyclerView.setHasFixedSize(true);

        /* Set the layout manager
        and adapter for items
        of the parent recyclerview */
        questionRecyclerView.setAdapter(questionAdapter);
        questionRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        reloadQuestions("");

        swipeRefreshLayout.setOnRefreshListener(() -> {
            reloadQuestions("");
            searchView.setQuery("", false);
            swipeRefreshLayout.setRefreshing(false);
        });

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if(!query.trim().isEmpty()) {
                    questionAdapter.clearAll();
                    reloadQuestions(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if(newText.trim().isEmpty()) {
                    reloadQuestions("");
                }
                return false;
            }
        });

//      insert new fragment to create a new post
        view.findViewById(R.id.addFab).setOnClickListener(v ->
                showDialog());
    }

    private void reloadQuestions(String query) {
        this.dao.get().addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                swipeRefreshLayout.setRefreshing(true);
                if(snapshot.exists()) {
                    questionAdapter.clearAll();
                    List<QuestionItem> questionItemList = new ArrayList<>();
                    for (var data : snapshot.getChildren()) {
                        var item = data.getValue(QuestionItem.class);
                        Objects.requireNonNull(item).setPostID(data.getKey());
                        questionItemList.add(item);
                    }
                    if(!query.trim().isEmpty()) {
                        questionAdapter.setQuestions(questionItemList
                                .stream()
                                .filter(e ->
                                        e.getQuestionTitle().contains(query)
                                                || e.getQuestionDescription().contains(query))
                                .collect(Collectors.toList()));

                    } else {
                        questionAdapter.setQuestions(questionItemList);
                    }
                    questionAdapter.notifyDataSetChanged();
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

        dialog.findViewById(R.id.addButton).setOnClickListener(v -> {
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

    @Override
    public void onItemClick(QuestionItem item) {
        var dialog = new Dialog(getActivity());
        Utils.showDialog(dialog, R.layout.list_dialog, R.style.dialog_horizontal_swipe_animation);

        RecyclerView questionRecyclerView = dialog.findViewById(R.id.recyclerList);
        questionRecyclerView.setHasFixedSize(true);

        /* Set the layout manager
        and adapter for items
        of the parent recyclerview */
        questionRecyclerView.setAdapter(replyAdapter);
        questionRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        SwipeRefreshLayout swipeRefreshLayout  = dialog.findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(() -> {
            reloadReplies(item.getPostID());
            swipeRefreshLayout.setRefreshing(false);
        });

        ViewStub stub = new ViewStub(getActivity(), R.layout.reply_add_component);
        LinearLayout linearLayout = dialog.findViewById(R.id.dialogListContainer);
        linearLayout.addView(stub);
        stub.inflate();

        loadReplies(item.getPostID());

        dialog.findViewById(R.id.addButton).setOnClickListener(v -> {
            TextView textView = dialog.findViewById(R.id.newText);
            if(textView != null) {
                if (currentUser == null) {
                    new FirebaseDAOUser().getUserInfo().addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            currentUser = snapshot.getValue(User.class);

                            var reply = new ReplyItem()
                                    .setUserID(Objects.requireNonNull(currentUser).getUserID())
                                    .setNickname(Objects.requireNonNull(currentUser).getNickname())
                                    .setReply(textView.getText().toString())
                                    .setTimestamp(-1 * new Date().getTime())
                                    .setUri(currentUser.getProfilePicture());

                            dao.addReply(item.getPostID(), reply).addOnCompleteListener(task -> {
                                textView.setText("");
                                if (task.isSuccessful()) {
                                    Toast.makeText(getActivity(), "post has been added successfully",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getActivity(), "Failed to submit new post! Try again",
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } else {
                    var reply = new ReplyItem()
                            .setUserID(currentUser.getUserID())
                            .setNickname(Objects.requireNonNull(currentUser).getNickname())
                            .setReply(textView.getText().toString())
                            .setTimestamp(-1 * new Date().getTime())
                            .setUri(currentUser.getProfilePicture());

                    dao.addReply(item.getPostID(), reply).addOnCompleteListener(task -> {
                        textView.setText("");
                        if (task.isSuccessful()) {
                            Toast.makeText(getActivity(), "post has been added successfully",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getActivity(), "Failed to submit new post! Try again",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void reloadReplies(String key) {
        replyAdapter.clearAll();

        dao.getReplies(key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    List<ReplyItem> replyItemList = new ArrayList<>();
                    for (var data : snapshot.getChildren()) {
                        replyItemList.add(data.getValue(ReplyItem.class));
                    }
                    replyAdapter.setReplies(replyItemList);
                    replyAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void loadReplies(String key) {
        replyAdapter.clearAll();
        dao.getReplies(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    List<ReplyItem> replyItemList = new ArrayList<>();
                    for (var data : snapshot.getChildren()) {
                        replyItemList.add(data.getValue(ReplyItem.class));
                    }
                    replyAdapter.setReplies(replyItemList);
                    replyAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}


