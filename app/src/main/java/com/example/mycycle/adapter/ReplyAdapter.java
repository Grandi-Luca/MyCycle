package com.example.mycycle.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.mycycle.R;
import com.example.mycycle.model.ReplyItem;

import java.util.ArrayList;
import java.util.List;

public class ReplyAdapter extends RecyclerView.Adapter<ReplyAdapter.ReplyViewHolder> {

    private final Context context;
    private List<ReplyItem> replies;

    public ReplyAdapter(Context context) {
        this.context = context;
        this.replies = new ArrayList<>();
    }

    public void setReplies(List<ReplyItem> replies) {
        this.replies = replies;
    }

    public void clearAll() {
        this.replies.clear();
    }

    public void addReply(ReplyItem item){
        this.replies.add(item);
    }

    public List<ReplyItem> getReplies() {
        return new ArrayList<>(replies);
    }

    @NonNull
    @Override
    public ReplyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.reply_item, parent, false);

        return new ReplyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReplyViewHolder holder, int position) {
        ReplyItem reply = new ArrayList<>(replies).get(position);

        holder.nickname.setText(reply.getNickname());
        holder.reply.setText(reply.getReply());
        Glide.with(context)
                .load(reply.getUri())
                .into(holder.profilePicture);
    }

    @Override
    public int getItemCount() {
        return replies.size();
    }

    public static class ReplyViewHolder extends RecyclerView.ViewHolder {

        private final TextView nickname;
        private final TextView reply;
        private final ImageView profilePicture;

        public ReplyViewHolder(@NonNull View itemView) {
            super(itemView);

            profilePicture = itemView.findViewById(R.id.profileImage);
            reply = itemView.findViewById(R.id.reply);
            nickname = itemView.findViewById(R.id.nickname);
        }
    }
}
