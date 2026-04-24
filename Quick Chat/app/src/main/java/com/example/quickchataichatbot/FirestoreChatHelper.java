package com.example.quickchataichatbot;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class FirestoreChatHelper {

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public FirestoreChatHelper() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public interface OnChatCreatedListener {
        void onSuccess(String chatId);
        void onFailure(Exception e);
    }

    public interface OnOperationListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    public interface OnMessagesLoadedListener {
        void onSuccess(com.google.firebase.firestore.QuerySnapshot snapshots);
        void onFailure(Exception e);
    }

    public interface OnChatsLoadedListener {
        void onSuccess(com.google.firebase.firestore.QuerySnapshot snapshots);
        void onFailure(Exception e);
    }

    public interface OnDeleteChatListener {
        void onSuccess();
        void onFailure(Exception e);
    }

    private FirebaseUser getCurrentUser() {
        return auth.getCurrentUser();
    }

    private CollectionReference getChatsRef() {
        FirebaseUser user = getCurrentUser();
        if (user == null) return null;

        return db.collection("Users")
                .document(user.getUid())
                .collection("Chats");
    }

    public void createNewChat(String title, OnChatCreatedListener listener) {
        CollectionReference chatsRef = getChatsRef();

        if (chatsRef == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }

        String chatId = chatsRef.document().getId();
        long now = System.currentTimeMillis();

        Map<String, Object> chatMap = new HashMap<>();
        chatMap.put("chatId", chatId);
        chatMap.put("title", title);
        chatMap.put("lastMessage", "");
        chatMap.put("createdAt", now);
        chatMap.put("updatedAt", now);

        chatsRef.document(chatId)
                .set(chatMap)
                .addOnSuccessListener(unused -> listener.onSuccess(chatId))
                .addOnFailureListener(listener::onFailure);
    }

    public void saveMessage(String chatId, Message message, OnOperationListener listener) {
        CollectionReference chatsRef = getChatsRef();

        if (chatsRef == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }

        String messageId = chatsRef.document(chatId)
                .collection("Messages")
                .document()
                .getId();

        chatsRef.document(chatId)
                .collection("Messages")
                .document(messageId)
                .set(message)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    public void updateChatSummary(String chatId, String title, String lastMessage, OnOperationListener listener) {
        CollectionReference chatsRef = getChatsRef();

        if (chatsRef == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }

        Map<String, Object> updateMap = new HashMap<>();
        updateMap.put("title", title);
        updateMap.put("lastMessage", lastMessage);
        updateMap.put("updatedAt", System.currentTimeMillis());

        chatsRef.document(chatId)
                .update(updateMap)
                .addOnSuccessListener(unused -> listener.onSuccess())
                .addOnFailureListener(listener::onFailure);
    }

    public void loadMessages(String chatId, OnMessagesLoadedListener listener) {
        CollectionReference chatsRef = getChatsRef();

        if (chatsRef == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }

        chatsRef.document(chatId)
                .collection("Messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(listener::onSuccess)
                .addOnFailureListener(listener::onFailure);
    }

    public void loadChats(OnChatsLoadedListener listener) {
        CollectionReference chatsRef = getChatsRef();

        if (chatsRef == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }

        chatsRef.orderBy("updatedAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(listener::onSuccess)
                .addOnFailureListener(listener::onFailure);
    }

    public void deleteChat(String chatId, OnDeleteChatListener listener) {
        CollectionReference chatsRef = getChatsRef();

        if (chatsRef == null) {
            listener.onFailure(new Exception("User not logged in"));
            return;
        }

        chatsRef.document(chatId)
                .collection("Messages")
                .get()
                .addOnSuccessListener(messageSnapshots -> {
                    if (messageSnapshots.isEmpty()) {
                        chatsRef.document(chatId)
                                .delete()
                                .addOnSuccessListener(unused -> listener.onSuccess())
                                .addOnFailureListener(listener::onFailure);
                        return;
                    }

                    final int totalMessages = messageSnapshots.size();
                    final int[] deletedCount = {0};
                    final boolean[] hasFailed = {false};

                    for (com.google.firebase.firestore.DocumentSnapshot messageDoc : messageSnapshots.getDocuments()) {
                        messageDoc.getReference()
                                .delete()
                                .addOnSuccessListener(unused -> {
                                    deletedCount[0]++;

                                    if (!hasFailed[0] && deletedCount[0] == totalMessages) {
                                        chatsRef.document(chatId)
                                                .delete()
                                                .addOnSuccessListener(unused2 -> listener.onSuccess())
                                                .addOnFailureListener(listener::onFailure);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    if (!hasFailed[0]) {
                                        hasFailed[0] = true;
                                        listener.onFailure(e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(listener::onFailure);
    }
}