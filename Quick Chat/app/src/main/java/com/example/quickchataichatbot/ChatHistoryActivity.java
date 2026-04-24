package com.example.quickchataichatbot;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ChatHistoryActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private RecyclerView recyclerViewChatHistory;
    private ChatHistoryAdapter chatHistoryAdapter;
    private List<ChatHistory> chatHistoryList;
    private View overlayCloseArea;
    private Button btnNewChatHistory;
    private EditText editTextSearchHistory;

    private FirebaseAuth mAuth;
    private FirestoreChatHelper firestoreChatHelper;
    private Dialog deleteDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_history);

        Window window = getWindow();
        window.setLayout(
                (int) (getResources().getDisplayMetrics().widthPixels * 1.0),
                WindowManager.LayoutParams.MATCH_PARENT
        );

        mAuth = FirebaseAuth.getInstance();
        firestoreChatHelper = new FirestoreChatHelper();

        btnBack = findViewById(R.id.btnBackHistory);
        recyclerViewChatHistory = findViewById(R.id.recyclerViewChatHistory);
        overlayCloseArea = findViewById(R.id.overlayCloseArea);
        btnNewChatHistory = findViewById(R.id.btnNewChatHistory);
        editTextSearchHistory = findViewById(R.id.editTextSearchHistory);

        btnBack.setOnClickListener(v -> finish());
        overlayCloseArea.setOnClickListener(v -> finish());

        recyclerViewChatHistory.setLayoutManager(new LinearLayoutManager(this));

        chatHistoryList = new ArrayList<>();
        chatHistoryAdapter = new ChatHistoryAdapter(
                chatHistoryList,
                chatHistory -> {
                    Intent intent = new Intent(ChatHistoryActivity.this, ChatApp.class);
                    intent.putExtra("chat_id", chatHistory.getChatId());
                    startActivity(intent);
                    finish();
                },
                (anchorView, chatHistory, position) -> showDeleteConfirmation(chatHistory, position)
        );

        recyclerViewChatHistory.setAdapter(chatHistoryAdapter);

        btnNewChatHistory.setOnClickListener(v -> {
            Intent intent = new Intent(ChatHistoryActivity.this, ChatApp.class);
            intent.putExtra("force_new_chat", true);
            startActivity(intent);
            finish();
        });

        editTextSearchHistory.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChats(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {
            }
        });

        loadChats();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadChats();
    }

    private void loadChats() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            chatHistoryList.clear();
            chatHistoryAdapter.notifyDataSetChanged();
            Toast.makeText(this, "Login to see chat history", Toast.LENGTH_SHORT).show();
            return;
        }

        firestoreChatHelper.loadChats(new FirestoreChatHelper.OnChatsLoadedListener() {
            @Override
            public void onSuccess(com.google.firebase.firestore.QuerySnapshot snapshots) {
                chatHistoryList.clear();

                for (QueryDocumentSnapshot document : snapshots) {
                    ChatHistory chatHistory = document.toObject(ChatHistory.class);
                    if (chatHistory != null) {
                        if (chatHistory.getChatId() == null || chatHistory.getChatId().trim().isEmpty()) {
                            chatHistory.setChatId(document.getId());
                        }
                        chatHistoryList.add(chatHistory);
                    }
                }

                chatHistoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ChatHistoryActivity.this, "Failed to load chats: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void filterChats(String query) {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        String searchText = query == null ? "" : query.trim().toLowerCase(Locale.getDefault());

        firestoreChatHelper.loadChats(new FirestoreChatHelper.OnChatsLoadedListener() {
            @Override
            public void onSuccess(com.google.firebase.firestore.QuerySnapshot snapshots) {
                chatHistoryList.clear();

                for (QueryDocumentSnapshot document : snapshots) {
                    ChatHistory chatHistory = document.toObject(ChatHistory.class);
                    if (chatHistory == null) continue;

                    if (chatHistory.getChatId() == null || chatHistory.getChatId().trim().isEmpty()) {
                        chatHistory.setChatId(document.getId());
                    }

                    String title = chatHistory.getTitle() != null
                            ? chatHistory.getTitle().toLowerCase(Locale.getDefault())
                            : "";

                    if (searchText.isEmpty() || title.contains(searchText)) {
                        chatHistoryList.add(chatHistory);
                    }
                }

                chatHistoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ChatHistoryActivity.this, "Search failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmation(ChatHistory chatHistory, int position) {
        if (deleteDialog != null && deleteDialog.isShowing()) {
            return;
        }

        deleteDialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_logout_confirm, null);

        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvDialogMessage = view.findViewById(R.id.tvDialogMessage);
        TextView btnCancelDelete = view.findViewById(R.id.btnCancelLogout);
        TextView btnConfirmDelete = view.findViewById(R.id.btnConfirmLogout);

        tvDialogTitle.setText("Delete Chat?");
        tvDialogMessage.setText("Do you want to delete this chat permanently?");
        btnConfirmDelete.setText("Delete");

        btnCancelDelete.setOnClickListener(v -> deleteDialog.dismiss());

        btnConfirmDelete.setOnClickListener(v -> {
            deleteDialog.dismiss();
            deleteChat(chatHistory, position);
        });

        deleteDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        deleteDialog.setContentView(view);
        deleteDialog.setCancelable(true);

        if (deleteDialog.getWindow() != null) {
            deleteDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        deleteDialog.show();

        if (deleteDialog.getWindow() != null) {
            deleteDialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void deleteChat(ChatHistory chatHistory, int position) {
        if (chatHistory == null || chatHistory.getChatId() == null || chatHistory.getChatId().trim().isEmpty()) {
            Toast.makeText(this, "Invalid chat", Toast.LENGTH_SHORT).show();
            return;
        }

        firestoreChatHelper.deleteChat(chatHistory.getChatId(), new FirestoreChatHelper.OnDeleteChatListener() {
            @Override
            public void onSuccess() {
                if (position >= 0 && position < chatHistoryList.size()) {
                    chatHistoryList.remove(position);
                    chatHistoryAdapter.notifyItemRemoved(position);
                } else {
                    loadChats();
                }

                Toast.makeText(ChatHistoryActivity.this, "Chat deleted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ChatHistoryActivity.this, "Delete failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (deleteDialog != null && deleteDialog.isShowing()) {
            deleteDialog.dismiss();
        }
        super.onDestroy();
    }
}