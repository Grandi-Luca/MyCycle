package com.example.mycycle.recycleView;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.example.mycycle.DAOPost;
import com.example.mycycle.R;
import com.example.mycycle.ReplyItem;
import com.example.mycycle.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private final Context context;
    private final Deque<QuestionItem> questions;
    private final DAOPost dao;
    private String replyKey;
    private boolean isLoading;
    private ReplyAdapter replyAdapter;

    public QuestionAdapter(Context context) {
        this.context = context;
        this.questions = new ArrayDeque<>();
        this.dao = new DAOPost("replies");
        this.replyKey = null;
        this.isLoading = false;
    }

    public void addQuestion(QuestionItem item) {
        this.questions.addFirst(item);
    }

    public void setQuestions(List<QuestionItem> questions) {
        this.questions.addAll(questions);
    }

    public void clearAll(){
        this.questions.clear();
    }

    private List<QuestionItem> getQuestions() {
        return new ArrayList<>(questions);
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.question_item, parent, false);

        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        /* Create an instance of the QuestionItem
        class for the given position */
        QuestionItem question = this.getQuestions().get(position);

        LinearLayoutManager layoutManager = new LinearLayoutManager(
                holder.replyRecyclerView.getContext(), LinearLayoutManager.VERTICAL, false);

        layoutManager.setInitialPrefetchItemCount(question.getQuestionReplies().size());
        replyAdapter = new ReplyAdapter(context);
        holder.replyRecyclerView.setLayoutManager(layoutManager);
        holder.replyRecyclerView.setHasFixedSize(true);
        holder.replyRecyclerView.setAdapter(replyAdapter);

        holder.title.setText(question.getQuestionTitle());
        holder.description.setText(question.getQuestionDescription());
        holder.nickname.setText(question.getNickname());
        Glide.with(context)
                .load(question.getUri())
                .into(holder.profilePicture);

        holder.expandLayout.setVisibility(View.VISIBLE);

        setArrowImage(holder, question.isExpand());
        holder.arrow.setOnClickListener(v -> {
            question.toggleExpand();
            setArrowImage(holder, question.isExpand());
//            holder.expandLayout.setVisibility(question.isExpand() ? View.VISIBLE : View.GONE);
            loadData(holder, false, question.getPostID());
        });

        holder.addBtn.setOnClickListener(v -> {
            FirebaseDatabase.getInstance("https://auth-89f75-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("users")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User currentUser = snapshot.getValue(User.class);
                            Objects.requireNonNull(currentUser)
                                    .setUserID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            var reply = new ReplyItem()
                                    .setUserID(currentUser.getUserID())
                                    .setPostID(question.getPostID())
                                    .setNickname(Objects.requireNonNull(currentUser).getNickname())
                                    .setReply(holder.newReply.getText().toString())
                                    .setTimestamp(-1 * new Date().getTime())
                                    .setUri(currentUser.getProfilePicture());

                            dao.addReply(reply).addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    Toast.makeText(context,
                                                    "post has been added successfully",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                } else {
                                    Toast.makeText(
                                                    context,
                                                    "Failed to submit new post! Try again",
                                                    Toast.LENGTH_LONG)
                                            .show();
                                }
                            });
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

        });

        holder.replyRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
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
                        loadData(holder, true, question.getPostID());
                    }
                }
            }
        });

    }

    private void setArrowImage(QuestionViewHolder holder, boolean isExpand) {
        holder.arrow.setImageResource(isExpand
                ? R.drawable.ic_baseline_arrow_drop_up_24
                : R.drawable.ic_baseline_arrow_drop_down_24);
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    private void loadData(QuestionViewHolder holder, boolean onScroll, String id) {
        holder.swipeRefreshLayout.setRefreshing(true);
        FirebaseDatabase
                .getInstance("https://auth-89f75-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("replies")
                .addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!onScroll) {
                    replyAdapter.clearAll();
                }
                final var questsList = new ArrayList<ReplyItem>();
                for (var data : snapshot.getChildren()){
                    ReplyItem reply = data.getValue(ReplyItem.class);
                    questsList.add(reply);
                    replyKey = String.valueOf(Objects.requireNonNull(reply).getTimestamp());
                }
                replyAdapter.setReplies(questsList);
                replyAdapter.notifyDataSetChanged();

                replyAdapter.getReplies().forEach(e -> System.err.println(e.toString()));

                isLoading = false;
                holder.swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                holder.swipeRefreshLayout.setRefreshing(false);
                throw error.toException();
            }
        });
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {

        private final TextView title, description, nickname;
        private final RecyclerView replyRecyclerView;
        private final ImageView profilePicture;
        private final LinearLayout expandLayout;
        private final ImageButton arrow;
        private final SwipeRefreshLayout swipeRefreshLayout;
        private final EditText newReply;
        private final MaterialButton addBtn;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePicture = itemView.findViewById(R.id.profileImage);
            nickname = itemView.findViewById(R.id.nickname);
            title = itemView.findViewById(R.id.questionTitle);
            description = itemView.findViewById(R.id.questionDescription);
            arrow = itemView.findViewById(R.id.arrowReply);
            expandLayout = itemView.findViewById(R.id.expandLayout);
            swipeRefreshLayout = itemView.findViewById(R.id.nestedSwipeLayout);
            replyRecyclerView = itemView.findViewById(R.id.replyList);
            newReply = itemView.findViewById(R.id.newReplyText);
            addBtn = itemView.findViewById(R.id.addNewReply);
        }
    }
}
