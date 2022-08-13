package com.example.mycycle.recycleView;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mycycle.QuestionItem;
import com.example.mycycle.R;
import com.example.mycycle.Utils;

import java.util.ArrayList;
import java.util.List;

public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.QuestionViewHolder> {

    /*
    An object of RecyclerView.RecycledViewPool is created to share the Views
    between the child and the parent RecyclerViews
    */
    private final Context context;
    private final RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
    private List<QuestionItem> questions;

    public QuestionAdapter(Context context) {
        this.context = context;
    }

    public void setQuestions(List<QuestionItem> questions) {
        this.questions = questions;
    }

    public List<QuestionItem> getQuestions() {
        return questions;
    }

    public void addAllQuests(List<QuestionItem> quests) {
        this.questions.addAll(quests);
    }

    public void clearAllQuests() {
        if(getItemCount() != 0){
            this.questions.clear();
        }
    }

    @NonNull
    @Override
    public QuestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        /* Here we inflate the corresponding
        layout of the parent item */
        View view = LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.question_item, parent, false);

        return new QuestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull QuestionViewHolder holder, int position) {
        /* Create an instance of the QuestionItem
        class for the given position */
        QuestionItem question = questions.get(position);

        holder.title.setText(question.getQuestionTitle());
        holder.description.setText(question.getQuestionDescription());
        holder.nickname.setText(question.getNickname());
        Glide.with(context)
                .load(question.getUri())
                .into(holder.profilePicture);

         /* Create a layout manager
         to assign a layout
         to the RecyclerView. */

         /* Here we have assigned the layout
         as LinearLayout with vertical orientation */
        LinearLayoutManager layoutManager = new LinearLayoutManager(
                holder.replies.getContext(), LinearLayoutManager.VERTICAL, false);

        /* Since this is a nested layout, so
        to define how many child items
        should be prefetched when the
        child RecyclerView is nested
        inside the parent RecyclerView,
        we use the following method */
        layoutManager.setInitialPrefetchItemCount(question.getQuestionReplies().size());

        /* Create an instance of the reply
        item view adapter and set its
        adapter, layout manager and RecyclerViewPool */
        ReplyAdapter replyAdapter = new ReplyAdapter(question.getQuestionReplies());
        holder.replies.setLayoutManager(layoutManager);
        holder.replies.setAdapter(replyAdapter);
        holder.replies.setRecycledViewPool(viewPool);

    }

    @Override
    public int getItemCount() {
        return questions == null ? 0 : questions.size();
    }

    static class QuestionViewHolder extends RecyclerView.ViewHolder {

        private final TextView title, description, nickname;
        private final RecyclerView replies;
        private final ImageView profilePicture;

        public QuestionViewHolder(@NonNull View itemView) {
            super(itemView);

            nickname = itemView.findViewById(R.id.nickname);
            title = itemView.findViewById(R.id.questionTitle);
            description = itemView.findViewById(R.id.questionDescription);
            replies = itemView.findViewById(R.id.replyList);
            profilePicture = itemView.findViewById(R.id.profileImage);
        }
    }
}
