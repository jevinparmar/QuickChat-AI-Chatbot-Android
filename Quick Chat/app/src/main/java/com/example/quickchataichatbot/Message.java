package com.example.quickchataichatbot;

public class Message {

    public static final int TYPE_USER = 1;
    public static final int TYPE_BOT = 2;
    public static final int TYPE_USER_FILE = 3;

    private String messageText;
    private int messageType;
    private String fileName;
    private String fileUri;
    private String mimeType;
    private String sender;
    private long timestamp;

    public Message() {
        // Required empty constructor for Firestore
    }

    public Message(String messageText, int messageType) {
        this.messageText = messageText;
        this.messageType = messageType;
        this.timestamp = System.currentTimeMillis();
    }

    public Message(String messageText, int messageType, String fileName, String fileUri, String mimeType) {
        this.messageText = messageText;
        this.messageType = messageType;
        this.fileName = fileName;
        this.fileUri = fileUri;
        this.mimeType = mimeType;
        this.timestamp = System.currentTimeMillis();
    }

    public Message(String messageText, int messageType, String fileName, String fileUri, String mimeType, String sender, long timestamp) {
        this.messageText = messageText;
        this.messageType = messageType;
        this.fileName = fileName;
        this.fileUri = fileUri;
        this.mimeType = mimeType;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public String getMessageText() {
        return messageText;
    }

    public int getMessageType() {
        return messageType;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileUri() {
        return fileUri;
    }

    public String getMimeType() {
        return mimeType;
    }

    public String getSender() {
        return sender;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setMessageType(int messageType) {
        this.messageType = messageType;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}