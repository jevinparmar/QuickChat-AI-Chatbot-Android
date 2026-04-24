package com.example.quickchataichatbot;

public class ChatHistory {

    private String chatId;
    private String title;
    private String lastMessage;
    private long updatedAt;

    public ChatHistory() {
    }

    public ChatHistory(String chatId, String title, String lastMessage, long updatedAt) {
        this.chatId = chatId;
        this.title = title;
        this.lastMessage = lastMessage;
        this.updatedAt = updatedAt;
    }

    public String getChatId() {
        return chatId;
    }

    public String getTitle() {
        return title;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public long getUpdatedAt() {
        return updatedAt;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setUpdatedAt(long updatedAt) {
        this.updatedAt = updatedAt;
    }
}