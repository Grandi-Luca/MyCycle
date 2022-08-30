package com.example.mycycle;

import static com.example.mycycle.MainActivity.currentUser;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class ForumFragment extends Fragment implements QuestionAdapter.OnItemListener {

    private QuestionAdapter questionAdapter;
    private ReplyAdapter replyAdapter;
    private DAOPost daoPost;
    private FirebaseDAOUser daoUser;

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
        this.daoPost = new DAOPost();
        this.daoUser = new FirebaseDAOUser();

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

        // show dialog to create a new post
        view.findViewById(R.id.addFab).setOnClickListener(v ->
                showAddNewQuestionDialog());
    }

    private void reloadQuestions(String query) {
        this.daoPost.get().addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                swipeRefreshLayout.setRefreshing(true);
                if(snapshot.exists()) {
                    questionAdapter.clearAll();
                    List<QuestionItem> questionItemList = new ArrayList<>();
                    for (var data : snapshot.getChildren()) {
                        var item = data.getValue(QuestionItem.class);
                        if(item != null) {
                            item.setPostID(data.getKey());
                            daoUser.getUserInfo(item.getUserID())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            var user = snapshot.getValue(User.class);
                                            if (user != null) {
                                                item.setNickname(user.getNickname())
                                                        .setUri(user.getProfilePicture());

                                                questionAdapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            swipeRefreshLayout.setRefreshing(false);
                                        }
                                    });

                        }
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

    private void showAddNewQuestionDialog() {
        var dialog = new Dialog(getActivity());
        Utils.showDialog(dialog, R.layout.add_new_post_dialog, R.style.dialog_down_top_swipe_animation);

        // add new question
        dialog.findViewById(R.id.addButton).setOnClickListener(v -> {
            var title = (EditText) dialog.findViewById(R.id.title);
            var description = (EditText) dialog.findViewById(R.id.questionDescription);

            if(description.getText() == null || description.getText().toString().trim().isEmpty()){
                return;
            }

            String titleText = title.getText() == null ? "" : title.getText().toString();
            var quest = new QuestionItem()
                    .setUserID(daoUser.getCurrentUid())
                    .setQuestionTitle(titleText)
                    .setQuestionDescription(description.getText().toString())
                    .setTimestamp(-1 * new Date().getTime());

            daoPost.addQuestion(quest).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(getActivity(), "Post has been added successfully",
                            Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), "Failed to submit new post! Try again",
                            Toast.LENGTH_LONG).show();
                }
            });
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

        ViewStub stub = new ViewStub(getActivity(), R.layout.reply_add_component);
        LinearLayout linearLayout = dialog.findViewById(R.id.dialogListContainer);
        linearLayout.addView(stub);
        stub.inflate();

        SwipeRefreshLayout swipeRefreshLayout = dialog.findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setOnRefreshListener(() ->
                swipeRefreshLayout.setRefreshing(false));

        loadReplies(item.getPostID());

        dialog.findViewById(R.id.addButton).setOnClickListener(v -> {
            TextView dialogTextView = dialog.findViewById(R.id.newText);
            if(dialogTextView  != null) {
                var reply = new ReplyItem()
                    .setUserID(currentUser.getUserID())
                    .setReply(dialogTextView.getText().toString())
                    .setTimestamp(-1 * new Date().getTime());

                daoPost.addReply(item.getPostID(), reply).addOnCompleteListener(task -> {
                    dialogTextView.setText("");
                    if (task.isSuccessful()) {
                        Toast.makeText(getActivity(), "Post has been added successfully",
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getActivity(), "Failed to submit new post! Try again",
                                Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    private void loadReplies(String key) {
        replyAdapter.clearAll();
        daoPost.getReplies(key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()) {
                    List<ReplyItem> replyItemList = new ArrayList<>();
                    for (var data : snapshot.getChildren()) {
                        var reply = data.getValue(ReplyItem.class);

                        if (reply != null) {
                            daoUser.getUserInfo(reply.getUserID())
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            var user = snapshot.getValue(User.class);
                                            if (user != null) {
                                                reply.setNickname(user.getNickname())
                                                        .setUri(user.getProfilePicture());

                                                replyAdapter.notifyDataSetChanged();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                        }
                        replyItemList.add(reply);
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


