package com.example.quickchataichatbot;

import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatApp extends AppCompatActivity {

    private RecyclerView recyclerViewChat;
    private EditText editTextMessage;
    private ImageButton btnSend;
    private ImageButton btnAttach;
    private ImageButton btnMenu;
    private TextView btnProfile;
    private LinearLayout emptyStateLayout;
    private LinearLayout topBar;
    private LinearLayout bottomInputContainer;
    private LinearLayout selectedImagePreviewContainer;
    private ImageView selectedImagePreview;
    private TextView tvSelectedImageLabel;
    private TextView tvSelectedImageSubLabel;

    private ChatAdapter chatAdapter;
    private ArrayList<Message> messageList;
    private LinearLayoutManager layoutManager;

    private int typingPosition = -1;
    private boolean isAwaitingAiResponse = false;

    private static final MediaType JSON =
            MediaType.parse("application/json; charset=utf-8");

    // Put your Groq API key here
    // ⚠️ IMPORTANT: Replace with your actual key from https://console.groq.com
    // Never commit your actual API key to GitHub!
    private static final String GROQ_API_KEY = "YOUR_GROQ_API_KEY_HERE";
    private static final String GROQ_CHAT_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String GROQ_TEXT_MODEL = "llama-3.3-70b-versatile";
    private static final String GROQ_VISION_MODEL = "meta-llama/llama-4-scout-17b-16e-instruct";
    private static final int MAX_RETRY_COUNT = 2;

    private static final String OWNER_REPLY =
            "I was created as a group project by:\n\n" +
                    "1. Jevin Parmar\n" +
                    "2. Savan Detroja\n" +
                    "3. Aryan Kapdiya\n\n" +
                    "This AI chat app was developed as part of our academic project. " +
                    "It is designed to answer questions, assist users, and demonstrate modern chat UI and AI integration.";

    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build();

    private Uri pendingFileUri = null;
    private String pendingFileName = null;
    private String pendingMimeType = null;

    private int guestMessageCount = 0;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirestoreChatHelper firestoreChatHelper;

    private String currentChatId = null;

    private final ActivityResultLauncher<String[]> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception ignored) {
                    }
                    handleSelectedFile(uri);
                }
            });

    private final ActivityResultLauncher<String[]> documentPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    try {
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    } catch (Exception ignored) {
                    }
                    handleSelectedFile(uri);
                }
            });

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        setContentView(R.layout.activity_chat_app);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        firestoreChatHelper = new FirestoreChatHelper();
        currentUser = mAuth.getCurrentUser();

        recyclerViewChat = findViewById(R.id.recyclerViewChat);
        editTextMessage = findViewById(R.id.editTextMessage);
        btnSend = findViewById(R.id.buttonSend);
        btnAttach = findViewById(R.id.btnAttach);
        btnMenu = findViewById(R.id.btnMenu);
        btnProfile = findViewById(R.id.btnProfile);
        emptyStateLayout = findViewById(R.id.emptyStateLayout);
        topBar = findViewById(R.id.topBar);
        bottomInputContainer = findViewById(R.id.bottomInputContainer);
        selectedImagePreviewContainer = findViewById(R.id.selectedImagePreviewContainer);
        selectedImagePreview = findViewById(R.id.selectedImagePreview);
        tvSelectedImageLabel = findViewById(R.id.tvSelectedImageLabel);
        tvSelectedImageSubLabel = findViewById(R.id.tvSelectedImageSubLabel);

        applyInsetsProperly();

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);

        layoutManager = new LinearLayoutManager(this);
        recyclerViewChat.setLayoutManager(layoutManager);
        recyclerViewChat.setAdapter(chatAdapter);

        updateEmptyState();
        updateMainProfileInitial();
        updateInputState(false);

        btnSend.setOnClickListener(v -> sendMessage());
        btnAttach.setOnClickListener(v -> {
            if (isAwaitingAiResponse) {
                Toast.makeText(this, "Please wait... QuickChat is replying", Toast.LENGTH_SHORT).show();
                return;
            }
            showAttachmentBottomSheet();
        });

        btnProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ChatApp.this, ProfileActivity.class);
            startActivity(intent);
        });

        btnMenu.setOnClickListener(v -> {
            Intent intent = new Intent(ChatApp.this, ChatHistoryActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_left, android.R.anim.fade_out);
        });

        editTextMessage.setOnEditorActionListener((v, actionId, event) -> {
            sendMessage();
            return true;
        });

        setupPasteSupport();
        handleChatOpenLogic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        currentUser = mAuth.getCurrentUser();
        updateMainProfileInitial();
    }

    private void handleChatOpenLogic() {
        currentUser = mAuth.getCurrentUser();

        String selectedChatId = getIntent().getStringExtra("chat_id");
        boolean forceNewChat = getIntent().getBooleanExtra("force_new_chat", false);

        if (currentUser == null) {
            currentChatId = null;
            messageList.clear();
            chatAdapter.notifyDataSetChanged();
            updateEmptyState();
            return;
        }

        if (!TextUtils.isEmpty(selectedChatId)) {
            currentChatId = selectedChatId;
            loadMessagesForChat(currentChatId);
            return;
        }

        if (forceNewChat || TextUtils.isEmpty(selectedChatId)) {
            currentChatId = null;
            messageList.clear();
            chatAdapter.notifyDataSetChanged();
            updateEmptyState();
        }
    }

    private void loadMessagesForChat(String chatId) {
        if (TextUtils.isEmpty(chatId)) {
            Toast.makeText(this, "Invalid chat id", Toast.LENGTH_SHORT).show();
            return;
        }

        firestoreChatHelper.loadMessages(chatId, new FirestoreChatHelper.OnMessagesLoadedListener() {
            @Override
            public void onSuccess(com.google.firebase.firestore.QuerySnapshot snapshots) {
                messageList.clear();

                for (com.google.firebase.firestore.DocumentSnapshot document : snapshots.getDocuments()) {
                    Message message = document.toObject(Message.class);
                    if (message != null) {
                        messageList.add(message);
                    }
                }

                chatAdapter.notifyDataSetChanged();
                updateEmptyState();
                scrollToBottomWithDelay();
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ChatApp.this, "Failed to load messages: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void updateMainProfileInitial() {
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            btnProfile.setText("?");
            return;
        }

        db.collection("Users")
                .document(currentUser.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String username = documentSnapshot.getString("username");

                        if (username == null || username.trim().isEmpty()) {
                            btnProfile.setText("U");
                        } else {
                            btnProfile.setText(getInitials(username));
                        }
                    } else {
                        btnProfile.setText("U");
                    }
                })
                .addOnFailureListener(e -> btnProfile.setText("U"));
    }

    private String getInitials(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "?";
        }

        String[] parts = name.trim().split("\\s+");

        if (parts.length == 1) {
            return String.valueOf(parts[0].charAt(0)).toUpperCase();
        }

        String first = parts[0].substring(0, 1).toUpperCase();
        String last = parts[parts.length - 1].substring(0, 1).toUpperCase();
        return first + last;
    }

    private void setupPasteSupport() {
        ViewCompat.setOnReceiveContentListener(
                editTextMessage,
                new String[]{"image/*"},
                (view, payload) -> {
                    if (isAwaitingAiResponse) {
                        Toast.makeText(this, "Please wait... QuickChat is replying", Toast.LENGTH_SHORT).show();
                        return payload;
                    }

                    ClipData clip = payload.getClip();
                    if (clip != null) {
                        for (int i = 0; i < clip.getItemCount(); i++) {
                            ClipData.Item item = clip.getItemAt(i);
                            Uri uri = item.getUri();
                            if (uri != null) {
                                handleSelectedFile(uri);
                                return null;
                            }
                        }
                    }
                    return payload;
                }
        );
    }

    private void showAttachmentBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_attachment, null);
        dialog.setContentView(sheetView);

        LinearLayout optionImage = sheetView.findViewById(R.id.optionImage);
        LinearLayout optionDocument = sheetView.findViewById(R.id.optionDocument);
        TextView btnCancel = sheetView.findViewById(R.id.btnCancelAttachment);

        optionImage.setOnClickListener(v -> {
            dialog.dismiss();
            imagePickerLauncher.launch(new String[]{"image/*"});
        });

        optionDocument.setOnClickListener(v -> {
            dialog.dismiss();
            documentPickerLauncher.launch(new String[]{
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "text/plain",
                    "*/*"
            });
        });

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void handleSelectedFile(Uri uri) {
        String fileName = getFileName(uri);
        String mimeType = getContentResolver().getType(uri);

        if (mimeType == null) {
            mimeType = "*/*";
        }

        pendingFileUri = uri;
        pendingFileName = fileName;
        pendingMimeType = mimeType;

        updateSelectedPreview();

        if (mimeType.startsWith("image/")) {
            Toast.makeText(this, "Image selected", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "File selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateSelectedPreview() {
        if (pendingFileUri != null && pendingMimeType != null && pendingMimeType.startsWith("image/")) {
            selectedImagePreviewContainer.setVisibility(View.VISIBLE);
            selectedImagePreview.setImageURI(pendingFileUri);

            tvSelectedImageLabel.setText("Selected image");

            if (pendingFileName != null && !pendingFileName.trim().isEmpty()) {
                tvSelectedImageSubLabel.setText(pendingFileName);
            } else {
                tvSelectedImageSubLabel.setText("Ready to send");
            }
        } else {
            selectedImagePreviewContainer.setVisibility(View.GONE);
            selectedImagePreview.setImageDrawable(null);
            tvSelectedImageLabel.setText("Selected image");
            tvSelectedImageSubLabel.setText("Ready to send");
        }
    }

    private String getFileName(Uri uri) {
        String result = "Selected file";

        if ("content".equals(uri.getScheme())) {
            Cursor cursor = null;
            try {
                cursor = getContentResolver().query(uri, null, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (nameIndex >= 0) {
                        result = cursor.getString(nameIndex);
                    }
                }
            } catch (Exception ignored) {
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        if (result == null || result.trim().isEmpty()) {
            result = "Selected file";
        }

        return result;
    }

    private void applyInsetsProperly() {
        View rootView = findViewById(android.R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (view, windowInsets) -> {
            Insets statusBars = windowInsets.getInsets(WindowInsetsCompat.Type.statusBars());
            Insets navigationBars = windowInsets.getInsets(WindowInsetsCompat.Type.navigationBars());
            Insets imeInsets = windowInsets.getInsets(WindowInsetsCompat.Type.ime());

            int extraTop = dpToPx(10);
            int sideSpace = dpToPx(16);
            int bottomSpace = Math.max(navigationBars.bottom, imeInsets.bottom);

            ViewGroup.MarginLayoutParams topBarParams =
                    (ViewGroup.MarginLayoutParams) topBar.getLayoutParams();
            topBarParams.leftMargin = sideSpace;
            topBarParams.rightMargin = sideSpace;
            topBarParams.topMargin = statusBars.top + extraTop;
            topBar.setLayoutParams(topBarParams);

            View rootInputArea = findViewById(R.id.inputAreaContainer);
            ViewGroup.MarginLayoutParams rootInputParams =
                    (ViewGroup.MarginLayoutParams) rootInputArea.getLayoutParams();
            rootInputParams.leftMargin = sideSpace;
            rootInputParams.rightMargin = sideSpace;
            rootInputParams.bottomMargin = dpToPx(14) + bottomSpace;
            rootInputArea.setLayoutParams(rootInputParams);

            ViewGroup.MarginLayoutParams previewParams =
                    (ViewGroup.MarginLayoutParams) selectedImagePreviewContainer.getLayoutParams();
            previewParams.bottomMargin = dpToPx(10);
            selectedImagePreviewContainer.setLayoutParams(previewParams);

            recyclerViewChat.setPadding(
                    dpToPx(12),
                    dpToPx(10),
                    dpToPx(12),
                    dpToPx(20)
            );

            emptyStateLayout.setPadding(
                    emptyStateLayout.getPaddingLeft(),
                    dpToPx(12),
                    emptyStateLayout.getPaddingRight(),
                    dpToPx(12)
            );

            rootInputArea.post(() -> {
                int inputHeight = rootInputArea.getHeight();
                int recyclerBottomPadding = inputHeight + bottomSpace + dpToPx(18);

                recyclerViewChat.setPadding(
                        dpToPx(12),
                        dpToPx(10),
                        dpToPx(12),
                        recyclerBottomPadding
                );
            });

            return windowInsets;
        });
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void hideKeyboard() {
        View current = getCurrentFocus();
        if (current == null) {
            current = editTextMessage;
        }

        InputMethodManager imm =
                (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        if (imm != null) {
            imm.hideSoftInputFromWindow(current.getWindowToken(), 0);
        }

        editTextMessage.clearFocus();
    }

    private void scrollToBottomWithDelay() {
        recyclerViewChat.postDelayed(() -> {
            if (!messageList.isEmpty()) {
                recyclerViewChat.smoothScrollToPosition(messageList.size() - 1);
            }
        }, 180);
    }

    private void showTyping() {
        messageList.add(new Message("Thinking...", Message.TYPE_BOT));
        typingPosition = messageList.size() - 1;
        chatAdapter.notifyItemInserted(typingPosition);
        scrollToBottomWithDelay();
    }

    private void updateInputState(boolean loading) {
        isAwaitingAiResponse = loading;

        btnSend.setEnabled(!loading);
        btnAttach.setEnabled(!loading);
        editTextMessage.setEnabled(!loading);

        btnSend.setAlpha(loading ? 0.5f : 1f);
        btnAttach.setAlpha(loading ? 0.5f : 1f);
        editTextMessage.setAlpha(loading ? 0.7f : 1f);

        if (loading) {
            editTextMessage.setHint("QuickChat is replying...");
        } else {
            editTextMessage.setHint("Ask QuickChat");
        }
    }

    private void sendMessage() {
        currentUser = mAuth.getCurrentUser();

        if (isAwaitingAiResponse) {
            Toast.makeText(this, "Please wait... QuickChat is replying", Toast.LENGTH_SHORT).show();
            return;
        }

        if (currentUser == null && guestMessageCount >= 3) {
            Toast.makeText(this, "Please login to continue chatting", Toast.LENGTH_LONG).show();
            startActivity(new Intent(ChatApp.this, LoginActivity.class));
            return;
        }

        String userText = editTextMessage.getText().toString().trim();

        boolean hasText = !TextUtils.isEmpty(userText);
        boolean hasPendingFile = pendingFileUri != null;

        if (!hasText && !hasPendingFile) {
            Toast.makeText(this, "Please enter a message or select an image", Toast.LENGTH_SHORT).show();
            return;
        }

        hideKeyboard();

        if (hasText && !hasPendingFile) {
            Message userMessage = new Message(userText, Message.TYPE_USER);
            userMessage.setSender("user");
            userMessage.setTimestamp(System.currentTimeMillis());

            addUserMessage(userMessage);

            if (currentUser == null) {
                guestMessageCount++;
            } else {
                saveMessageToFirestore(userMessage, getChatTitleFromMessage(userText), userText);
            }

            editTextMessage.setText("");

            if (isOwnerQuery(userText)) {
                Message ownerMessage = new Message(OWNER_REPLY, Message.TYPE_BOT);
                ownerMessage.setSender("bot");
                ownerMessage.setTimestamp(System.currentTimeMillis());

                addBotMessage(ownerMessage);

                if (currentUser != null) {
                    saveMessageToFirestore(ownerMessage, getChatTitleFromMessage(userText), OWNER_REPLY);
                }
                return;
            }

            showTyping();
            updateInputState(true);
            callGroqTextApi(userText, 0);
            return;
        }

        String caption = userText;
        editTextMessage.setText("");

        Message fileMessage = new Message(
                caption,
                Message.TYPE_USER_FILE,
                pendingFileName != null ? pendingFileName : "Selected file",
                pendingFileUri.toString(),
                pendingMimeType != null ? pendingMimeType : "*/*"
        );
        fileMessage.setSender("user");
        fileMessage.setTimestamp(System.currentTimeMillis());

        addUserFileMessage(fileMessage);

        if (currentUser == null) {
            guestMessageCount++;
        } else {
            String summaryText = TextUtils.isEmpty(caption)
                    ? "File: " + fileMessage.getFileName()
                    : caption;

            saveMessageToFirestore(fileMessage, getChatTitleFromMessage(summaryText), summaryText);
        }

        if (pendingMimeType != null && pendingMimeType.startsWith("image/")) {
            String promptToSend = TextUtils.isEmpty(caption)
                    ? "Describe this image clearly and simply."
                    : caption;

            Uri imageUri = pendingFileUri;
            String mimeType = pendingMimeType;
            clearPendingFile();

            showTyping();
            updateInputState(true);
            callGroqVisionApi(promptToSend, imageUri, mimeType, 0);
        } else {
            Message botMessage = new Message(
                    "The file was attached successfully, but AI analysis is currently available only for images.",
                    Message.TYPE_BOT
            );
            botMessage.setSender("bot");
            botMessage.setTimestamp(System.currentTimeMillis());

            addBotMessage(botMessage);

            if (currentUser != null) {
                saveMessageToFirestore(botMessage, "New Chat", botMessage.getMessageText());
            }

            clearPendingFile();
        }
    }

    private void saveMessageToFirestore(Message message, String title, String lastMessage) {
        if (TextUtils.isEmpty(currentChatId)) {
            createNewChatThenSave(message, title, lastMessage);
            return;
        }

        firestoreChatHelper.saveMessage(currentChatId, message, new FirestoreChatHelper.OnOperationListener() {
            @Override
            public void onSuccess() {
                firestoreChatHelper.updateChatSummary(currentChatId, title, lastMessage, new FirestoreChatHelper.OnOperationListener() {
                    @Override
                    public void onSuccess() {
                    }

                    @Override
                    public void onFailure(Exception e) {
                    }
                });
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ChatApp.this, "Failed to save message: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createNewChatThenSave(Message message, String title, String lastMessage) {
        firestoreChatHelper.createNewChat(title, new FirestoreChatHelper.OnChatCreatedListener() {
            @Override
            public void onSuccess(String chatId) {
                currentChatId = chatId;
                saveMessageToFirestore(message, title, lastMessage);
            }

            @Override
            public void onFailure(Exception e) {
                Toast.makeText(ChatApp.this, "Failed to create chat: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private String getChatTitleFromMessage(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "New Chat";
        }

        String cleaned = text.trim();
        if (cleaned.length() > 30) {
            return cleaned.substring(0, 30) + "...";
        }
        return cleaned;
    }

    private boolean isOwnerQuery(String text) {
        if (text == null) return false;

        String msg = text.toLowerCase(Locale.ROOT).trim();

        return msg.contains("who made you")
                || msg.contains("who created you")
                || msg.contains("who developed you")
                || msg.contains("who is your owner")
                || msg.contains("who's your owner")
                || msg.contains("who built you")
                || msg.contains("who is behind you")
                || msg.contains("who are your creators")
                || msg.contains("who is your creator")
                || msg.contains("who made this app")
                || msg.contains("who created this app")
                || msg.contains("who developed this app")
                || msg.contains("who is the owner of this app");
    }

    private void clearPendingFile() {
        pendingFileUri = null;
        pendingFileName = null;
        pendingMimeType = null;
        updateSelectedPreview();
    }

    private void addUserMessage(Message message) {
        messageList.add(message);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        updateEmptyState();
        scrollToBottomWithDelay();
    }

    private void addUserFileMessage(Message message) {
        messageList.add(message);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        updateEmptyState();
        scrollToBottomWithDelay();
    }

    private void callGroqTextApi(String userMessage, int retryCount) {
        try {
            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are QuickChat, a helpful AI assistant inside an Android app. Give clear, friendly, concise answers.");

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", userMessage);

            JSONArray messages = new JSONArray();
            messages.put(systemMessage);
            messages.put(userMsg);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", GROQ_TEXT_MODEL);
            jsonBody.put("messages", messages);
            jsonBody.put("temperature", 0.7);
            jsonBody.put("max_tokens", 500);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(GROQ_CHAT_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + GROQ_API_KEY)
                    .build();

            client.newCall(request).enqueue(new GroqCallback(userMessage, retryCount, false, null, null));
        } catch (Exception e) {
            replaceTypingWithMessage(getCustomErrorMessage(0, e.getMessage()), userMessage);
            updateInputState(false);
        }
    }

    private void callGroqVisionApi(String userPrompt, Uri imageUri, String mimeType, int retryCount) {
        try {
            byte[] imageBytes = readBytesFromUri(imageUri);
            String base64Image = Base64.encodeToString(imageBytes, Base64.NO_WRAP);
            String dataUrl = "data:" + mimeType + ";base64," + base64Image;

            JSONObject systemMessage = new JSONObject();
            systemMessage.put("role", "system");
            systemMessage.put("content", "You are QuickChat, a helpful AI assistant inside an Android app. Analyze images and answer clearly.");

            JSONArray contentParts = new JSONArray();

            JSONObject textPart = new JSONObject();
            textPart.put("type", "text");
            textPart.put("text", userPrompt);
            contentParts.put(textPart);

            JSONObject imageUrlObj = new JSONObject();
            imageUrlObj.put("url", dataUrl);
            imageUrlObj.put("detail", "auto");

            JSONObject imagePart = new JSONObject();
            imagePart.put("type", "image_url");
            imagePart.put("image_url", imageUrlObj);
            contentParts.put(imagePart);

            JSONObject userMsg = new JSONObject();
            userMsg.put("role", "user");
            userMsg.put("content", contentParts);

            JSONArray messages = new JSONArray();
            messages.put(systemMessage);
            messages.put(userMsg);

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("model", GROQ_VISION_MODEL);
            jsonBody.put("messages", messages);
            jsonBody.put("temperature", 0.5);
            jsonBody.put("max_completion_tokens", 700);

            RequestBody body = RequestBody.create(jsonBody.toString(), JSON);

            Request request = new Request.Builder()
                    .url(GROQ_CHAT_URL)
                    .post(body)
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Authorization", "Bearer " + GROQ_API_KEY)
                    .build();

            client.newCall(request).enqueue(new GroqCallback(userPrompt, retryCount, true, imageUri, mimeType));
        } catch (Exception e) {
            replaceTypingWithMessage(getCustomErrorMessage(0, e.getMessage()), userPrompt);
            updateInputState(false);
        }
    }

    private byte[] readBytesFromUri(Uri uri) throws IOException {
        InputStream inputStream = getContentResolver().openInputStream(uri);

        if (inputStream == null) {
            throw new IOException("Unable to open selected image");
        }

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[8192];
        int nRead;

        try {
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            return buffer.toByteArray();
        } finally {
            inputStream.close();
            buffer.close();
        }
    }

    private boolean shouldRetry(int statusCode, String errorText) {
        String error = errorText == null ? "" : errorText.toLowerCase(Locale.ROOT);
        return statusCode == 429 || statusCode == 500 || statusCode == 502 || statusCode == 503 || statusCode == 504
                || error.contains("rate limit")
                || error.contains("temporarily")
                || error.contains("overloaded")
                || error.contains("server had an error");
    }

    private void retryRequestIfPossible(String originalUserMessage, int retryCount, boolean isImageRequest, Uri imageUri, String mimeType) {
        if (retryCount >= MAX_RETRY_COUNT) {
            replaceTypingWithMessage(
                    "QuickChat is busy right now because the AI service is getting too many requests.\n\nPlease wait a moment and try again.",
                    originalUserMessage
            );
            updateInputState(false);
            return;
        }

        int delayMillis = (retryCount + 1) * 2000;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (isImageRequest) {
                callGroqVisionApi(originalUserMessage, imageUri, mimeType, retryCount + 1);
            } else {
                callGroqTextApi(originalUserMessage, retryCount + 1);
            }
        }, delayMillis);
    }

    private String getCustomErrorMessage(int statusCode, String errorMessage) {
        String error = errorMessage == null ? "" : errorMessage.toLowerCase(Locale.ROOT);

        if (statusCode == 401 || error.contains("incorrect api key") || error.contains("invalid_api_key")) {
            return "QuickChat could not connect because the Groq API key is invalid.\n\nPlease check your API key.";
        }

        if (statusCode == 429 || error.contains("rate limit") || error.contains("quota")) {
            return "QuickChat is temporarily busy because too many AI requests were sent.\n\nPlease wait a few seconds and try again.";
        }

        if (statusCode == 402 || error.contains("billing") || error.contains("payment")) {
            return "QuickChat could not continue because the Groq project billing or balance needs attention.";
        }

        if (statusCode == 500 || statusCode == 502 || statusCode == 503 || statusCode == 504) {
            return "QuickChat could not get a reply because the AI server is temporarily unavailable.\n\nPlease try again shortly.";
        }

        if (error.contains("failed to connect") || error.contains("timeout") || error.contains("unable to resolve host") || error.contains("network")) {
            return "QuickChat could not connect to the internet.\n\nPlease check your network and try again.";
        }

        if (statusCode == 400) {
            return "QuickChat sent a request the AI service could not process.\n\nPlease try a different message or image.";
        }

        return "QuickChat could not complete your request right now.\n\nPlease try again in a moment.";
    }

    private class GroqCallback implements Callback {
        private final String originalUserMessage;
        private final int retryCount;
        private final boolean isImageRequest;
        private final Uri imageUri;
        private final String mimeType;

        GroqCallback(String originalUserMessage, int retryCount, boolean isImageRequest, Uri imageUri, String mimeType) {
            this.originalUserMessage = originalUserMessage;
            this.retryCount = retryCount;
            this.isImageRequest = isImageRequest;
            this.imageUri = imageUri;
            this.mimeType = mimeType;
        }

        @Override
        public void onFailure(@NonNull Call call, @NonNull IOException e) {
            runOnUiThread(() -> {
                String errorText = e.getMessage() == null ? "Network error" : e.getMessage();
                replaceTypingWithMessage(getCustomErrorMessage(0, errorText), originalUserMessage);
                updateInputState(false);
            });
        }

        @Override
        public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
            String responseBody = response.body() != null ? response.body().string() : "";
            int code = response.code();

            try {
                JSONObject json = new JSONObject(responseBody);

                if (!response.isSuccessful()) {
                    String errorMessage = extractGroqError(json);
                    runOnUiThread(() -> {
                        if (shouldRetry(code, errorMessage)) {
                            retryRequestIfPossible(originalUserMessage, retryCount, isImageRequest, imageUri, mimeType);
                        } else {
                            replaceTypingWithMessage(getCustomErrorMessage(code, errorMessage), originalUserMessage);
                            updateInputState(false);
                        }
                    });
                    return;
                }

                String reply = extractAssistantText(json);
                if (TextUtils.isEmpty(reply)) {
                    reply = "QuickChat received an empty reply from the AI service. Please try again.";
                }

                String finalReply = reply;
                runOnUiThread(() -> {
                    replaceTypingWithMessage(finalReply, originalUserMessage);
                    updateInputState(false);
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    replaceTypingWithMessage(
                            "QuickChat could not read the AI response properly.\n\nPlease try again.",
                            originalUserMessage
                    );
                    updateInputState(false);
                });
            }
        }
    }

    private String extractGroqError(JSONObject json) {
        try {
            if (json.has("error")) {
                Object errorObj = json.get("error");
                if (errorObj instanceof JSONObject) {
                    JSONObject errorJson = (JSONObject) errorObj;
                    String msg = errorJson.optString("message");
                    if (msg != null && !msg.trim().isEmpty()) {
                        return msg;
                    }
                    String code = errorJson.optString("code");
                    if (code != null && !code.trim().isEmpty()) {
                        return code;
                    }
                } else if (errorObj instanceof String) {
                    return (String) errorObj;
                }
            }
        } catch (Exception ignored) {
        }
        return "Unknown Groq API error";
    }

    private String extractAssistantText(JSONObject json) {
        JSONArray choices = json.optJSONArray("choices");
        if (choices == null || choices.length() == 0) {
            return "";
        }

        JSONObject firstChoice = choices.optJSONObject(0);
        if (firstChoice == null) {
            return "";
        }

        JSONObject message = firstChoice.optJSONObject("message");
        if (message == null) {
            return "";
        }

        Object content = message.opt("content");
        if (content instanceof String) {
            return ((String) content).trim();
        }

        if (content instanceof JSONArray) {
            JSONArray parts = (JSONArray) content;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < parts.length(); i++) {
                JSONObject part = parts.optJSONObject(i);
                if (part == null) continue;
                String text = part.optString("text");
                if (!TextUtils.isEmpty(text)) {
                    if (builder.length() > 0) builder.append("\n");
                    builder.append(text);
                }
            }
            return builder.toString().trim();
        }

        return "";
    }

    private void replaceTypingWithMessage(String text, String originalUserMessage) {
        Message botMessage = new Message(text, Message.TYPE_BOT);
        botMessage.setSender("bot");
        botMessage.setTimestamp(System.currentTimeMillis());

        if (typingPosition != -1 && typingPosition < messageList.size()) {
            messageList.set(typingPosition, botMessage);
            chatAdapter.notifyItemChanged(typingPosition);
            typingPosition = -1;
        } else {
            messageList.add(botMessage);
            chatAdapter.notifyItemInserted(messageList.size() - 1);
        }

        if (currentUser != null) {
            saveMessageToFirestore(botMessage, getChatTitleFromMessage(originalUserMessage), text);
        }

        updateEmptyState();
        scrollToBottomWithDelay();
    }

    private void addBotMessage(Message message) {
        messageList.add(message);
        chatAdapter.notifyItemInserted(messageList.size() - 1);
        updateEmptyState();
        scrollToBottomWithDelay();
    }

    private void updateEmptyState() {
        if (messageList == null || messageList.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);
        } else {
            emptyStateLayout.setVisibility(View.GONE);
        }
    }
}
