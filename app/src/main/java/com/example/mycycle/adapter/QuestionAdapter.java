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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mycycle.R;
import com.example.mycycle.model.QuestionItem;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    private final Context context;
    private final OnItemListener itemListener;

    private List<QuestionItem> questions;

    public QuestionAdapter(Context context, OnItemListener itemListener) {
        this.context = context;
        this.questions = new ArrayList<>();

        this.itemListener = itemListener;
    }

    public void addQuestion(QuestionItem item) {
        this.questions.add(item);
    }

    public void setQuestions(List<QuestionItem> questions) {
        this.questions = questions;
    }

    public void clearAll(){
        this.questions.clear();
    }

    public List<QuestionItem> getQuestions() {
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

        holder.title.setText(question.getQuestionTitle());
        holder.description.setText(question.getQuestionDescription());
        holder.nickname.setText(question.getNickname());
        Glide.with(context)
                .load(question.getUri())
                .into(holder.profilePicture);

        holder.arrow.setOnClickListener(v -> itemListener.onItemClick(question));
    }

    @Override
    public int getItemCount() {
        return questions.size();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {

        private final TextView title, description, nickname;
        private final ImageView profilePicture;
        private final ImageButton arrow;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePicture = itemView.findViewById(R.id.profileImage);
            nickname = itemView.findViewById(R.id.nickname);
            title = itemView.findViewById(R.id.questionTitle);
            description = itemView.findViewById(R.id.questionDescription);
            arrow = itemView.findViewById(R.id.arrowReply);
        }
    }

    public interface OnItemListener {
        void onItemClick(QuestionItem item);
    }
}
