package com.example.quickchataichatbot;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.InputStream;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Message> messageList;

    public ChatAdapter(List<Message> messageList) {
        this.messageList = messageList;
    }

    @Override
    public int getItemViewType(int position) {
        return messageList.get(position).getMessageType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == Message.TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_message, parent, false);
            return new UserMessageViewHolder(view);

        } else if (viewType == Message.TYPE_USER_FILE) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_user_file_message, parent, false);
            return new UserFileMessageViewHolder(view);

        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_bot_message, parent, false);
            return new BotMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = messageList.get(position);

        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).textViewUserMessage.setText(
                    message.getMessageText() != null ? message.getMessageText() : ""
            );

        } else if (holder instanceof BotMessageViewHolder) {
            ((BotMessageViewHolder) holder).textViewBotMessage.setText(
                    message.getMessageText() != null ? message.getMessageText() : ""
            );

        } else if (holder instanceof UserFileMessageViewHolder) {
            UserFileMessageViewHolder fileHolder = (UserFileMessageViewHolder) holder;

            String fileTitle = !TextUtils.isEmpty(message.getFileName())
                    ? message.getFileName()
                    : "Selected file";

            String fileType = !TextUtils.isEmpty(message.getMimeType())
                    ? message.getMimeType()
                    : "Unknown type";

            fileHolder.textViewFileName.setText(fileTitle);
            fileHolder.textViewFileType.setText(fileType);

            fileHolder.imagePreview.setImageDrawable(null);
            fileHolder.imagePreview.setVisibility(View.GONE);
            fileHolder.fileIcon.setVisibility(View.VISIBLE);

            boolean isImage = fileType.startsWith("image/");
            String fileUriString = message.getFileUri();

            if (isImage && !TextUtils.isEmpty(fileUriString)) {
                try {
                    Uri imageUri = Uri.parse(fileUriString);
                    InputStream inputStream = fileHolder.itemView.getContext()
                            .getContentResolver()
                            .openInputStream(imageUri);

                    if (inputStream != null) {
                        Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                        inputStream.close();

                        if (bitmap != null) {
                            fileHolder.imagePreview.setVisibility(View.VISIBLE);
                            fileHolder.fileIcon.setVisibility(View.GONE);
                            fileHolder.imagePreview.setImageBitmap(bitmap);
                            fileHolder.textViewFileType.setText("Image");
                        } else {
                            showUnavailableImage(fileHolder);
                        }
                    } else {
                        showUnavailableImage(fileHolder);
                    }
                } catch (Exception e) {
                    showUnavailableImage(fileHolder);
                }
            } else {
                if (fileType.contains("pdf")) {
                    fileHolder.fileIcon.setImageResource(android.R.drawable.ic_menu_save);
                } else if (fileType.contains("word") || fileType.contains("document")) {
                    fileHolder.fileIcon.setImageResource(android.R.drawable.ic_menu_edit);
                } else {
                    fileHolder.fileIcon.setImageResource(android.R.drawable.ic_menu_agenda);
                }
            }

            if (!TextUtils.isEmpty(message.getMessageText())) {
                fileHolder.textViewOptionalCaption.setVisibility(View.VISIBLE);
                fileHolder.textViewOptionalCaption.setText(message.getMessageText());
            } else {
                fileHolder.textViewOptionalCaption.setVisibility(View.GONE);
            }

            fileHolder.itemView.setOnClickListener(v -> {
                String fileUri = message.getFileUri();
                String mimeType = message.getMimeType();

                if (TextUtils.isEmpty(fileUri)) {
                    Toast.makeText(v.getContext(), "File not available", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(fileUri), !TextUtils.isEmpty(mimeType) ? mimeType : "*/*");
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    v.getContext().startActivity(intent);
                } catch (Exception e) {
                    Toast.makeText(v.getContext(), "Unable to open this file/image", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void showUnavailableImage(UserFileMessageViewHolder fileHolder) {
        fileHolder.imagePreview.setVisibility(View.GONE);
        fileHolder.fileIcon.setVisibility(View.VISIBLE);
        fileHolder.fileIcon.setImageResource(android.R.drawable.ic_menu_gallery);
        fileHolder.textViewFileType.setText("Image preview unavailable");
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUserMessage;

        public UserMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUserMessage = itemView.findViewById(R.id.textViewUserMessage);
        }
    }

    static class BotMessageViewHolder extends RecyclerView.ViewHolder {
        TextView textViewBotMessage;

        public BotMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewBotMessage = itemView.findViewById(R.id.textViewBotMessage);
        }
    }

    static class UserFileMessageViewHolder extends RecyclerView.ViewHolder {
        ImageView imagePreview;
        ImageView fileIcon;
        TextView textViewFileName;
        TextView textViewFileType;
        TextView textViewOptionalCaption;

        public UserFileMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            imagePreview = itemView.findViewById(R.id.imagePreview);
            fileIcon = itemView.findViewById(R.id.fileIcon);
            textViewFileName = itemView.findViewById(R.id.textViewFileName);
            textViewFileType = itemView.findViewById(R.id.textViewFileType);
            textViewOptionalCaption = itemView.findViewById(R.id.textViewOptionalCaption);
        }
    }
}