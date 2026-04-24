package com.example.quickchataichatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatHistoryAdapter extends RecyclerView.Adapter<ChatHistoryAdapter.ChatHistoryViewHolder> {

    public interface OnChatClickListener {
        void onChatClick(ChatHistory chatHistory);
    }

    public interface OnChatLongClickListener {
        void onChatLongClick(View anchorView, ChatHistory chatHistory, int position);
    }

    private final List<ChatHistory> chatHistoryList;
    private final OnChatClickListener clickListener;
    private final OnChatLongClickListener longClickListener;

    public ChatHistoryAdapter(
            List<ChatHistory> chatHistoryList,
            OnChatClickListener clickListener,
            OnChatLongClickListener longClickListener
    ) {
        this.chatHistoryList = chatHistoryList;
        this.clickListener = clickListener;
        this.longClickListener = longClickListener;
    }

    @NonNull
    @Override
    public ChatHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_history, parent, false);
        return new ChatHistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatHistoryViewHolder holder, int position) {
        ChatHistory chatHistory = chatHistoryList.get(position);

        String title = chatHistory.getTitle();
        if (title == null || title.trim().isEmpty()) {
            title = "New Chat";
        }

        holder.tvChatTitle.setText(title);
        holder.tvChatTime.setText(formatTime(chatHistory.getUpdatedAt()));

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onChatClick(chatHistory);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (longClickListener != null) {
                int adapterPosition = holder.getBindingAdapterPosition();
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    longClickListener.onChatLongClick(v, chatHistory, adapterPosition);
                    return true;
                }
            }
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return chatHistoryList.size();
    }

    private String formatTime(long timeMillis) {
        if (timeMillis <= 0) {
            return "";
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault());
        return sdf.format(new Date(timeMillis));
    }

    static class ChatHistoryViewHolder extends RecyclerView.ViewHolder {
        TextView tvChatTitle, tvChatTime;

        public ChatHistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChatTitle = itemView.findViewById(R.id.tvChatTitle);
            tvChatTime = itemView.findViewById(R.id.tvChatTime);
        }
    }
}