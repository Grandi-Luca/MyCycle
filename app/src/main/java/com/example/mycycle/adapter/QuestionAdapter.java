package com.example.mycycle.adapter;

import android.content.Context;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mycycle.repo.DAOPost;
import com.example.mycycle.R;
import com.example.mycycle.model.ReplyItem;
import com.example.mycycle.model.QuestionItem;
import com.example.mycycle.model.User;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("unused")
public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private final Context context;
    private final List<QuestionItem> questions;
    private final DAOPost dao;
    private final RecyclerView.RecycledViewPool viewPool
            = new RecyclerView.RecycledViewPool();

    public QuestionAdapter(Context context) {
        this.context = context;
        this.questions = new ArrayList<>();
        this.dao = new DAOPost();
    }

    public void addQuestion(QuestionItem item) {
        this.questions.add(item);
    }

    public void setQuestions(List<QuestionItem> questions) {
        this.questions.addAll(questions);
    }

    public void clearAll(){
        this.questions.clear();
    }

    private List<QuestionItem> getQuestions() {
        return questions;
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

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(holder.replyRecyclerView.getContext(),
                LinearLayoutManager.VERTICAL, false);

        layoutManager.setInitialPrefetchItemCount(question.getQuestionReplies().size());
        ReplyAdapter replyAdapter = new ReplyAdapter(context);
        replyAdapter.setReplies(question.getQuestionReplies());
        holder.replyRecyclerView.setLayoutManager(layoutManager);
        holder.replyRecyclerView.setHasFixedSize(true);
        holder.replyRecyclerView.setAdapter(replyAdapter);
        holder.replyRecyclerView.setRecycledViewPool(viewPool);

        holder.title.setText(question.getQuestionTitle());
        holder.description.setText(question.getQuestionDescription());
        holder.nickname.setText(question.getNickname());
        Glide.with(context)
                .load(question.getUri())
                .into(holder.profilePicture);

        holder.expandLayout.setVisibility(View.GONE);

        holder.arrow.setOnClickListener(v -> {
            boolean isOpen = holder.expandLayout.getVisibility() == View.VISIBLE;
            holder.expandLayout.setVisibility(isOpen ? View.GONE : View.VISIBLE);
            holder.arrow.setImageResource(isOpen
                    ? R.drawable.ic_baseline_arrow_drop_down_24
                    : R.drawable.ic_baseline_arrow_drop_up_24);
        });

        holder.addBtn.setOnClickListener(v -> {
            if(holder.newReply.getText().toString().trim().isEmpty()) {
                return;
            }
            FirebaseDatabase.getInstance("https://auth-89f75-default-rtdb.europe-west1.firebasedatabase.app/")
                    .getReference("users")
                    .child(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid())
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            var currentUser = snapshot.getValue(User.class);
                            Objects.requireNonNull(currentUser)
                                    .setUserID(FirebaseAuth.getInstance().getCurrentUser().getUid());
                            var reply = new ReplyItem()
                                    .setUserID(currentUser.getUserID())
                                    .setNickname(Objects.requireNonNull(currentUser).getNickname())
                                    .setReply(holder.newReply.getText().toString())
                                    .setTimestamp(-1 * new Date().getTime())
                                    .setUri(currentUser.getProfilePicture());

                            dao.addReply(question.getPostID(), reply).addOnCompleteListener(task -> {
                                holder.newReply.setText("");
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

    static class QuestionViewHolder extends RecyclerView.ViewHolder {

        private final TextView title, description, nickname;
        private final RecyclerView replyRecyclerView;
        private final ImageView profilePicture;
        private final LinearLayout expandLayout;
        private final ImageButton arrow;
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
            replyRecyclerView = itemView.findViewById(R.id.replyList);
            newReply = itemView.findViewById(R.id.newReplyText);
            addBtn = itemView.findViewById(R.id.addNewReply);
        }
    }
}
