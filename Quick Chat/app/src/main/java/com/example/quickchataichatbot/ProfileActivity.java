package com.example.quickchataichatbot;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileActivity extends AppCompatActivity {

    private ImageButton btnBack;
    private TextView tvInitial;
    private TextView tvName;
    private TextView tvNameInfo;
    private TextView tvEmailInfo;
    private Button btnLoginProfile;
    private Button btnSignupProfile;
    private Button btnLogout;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    private Dialog loadingDialog;
    private Dialog logoutDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        btnBack = findViewById(R.id.btnBackProfile);
        tvInitial = findViewById(R.id.tvInitial);
        tvName = findViewById(R.id.tvName);
        tvNameInfo = findViewById(R.id.tvNameInfo);
        tvEmailInfo = findViewById(R.id.tvEmailInfo);
        btnLoginProfile = findViewById(R.id.btnLoginProfile);
        btnSignupProfile = findViewById(R.id.btnSignupProfile);
        btnLogout = findViewById(R.id.btnLogout);

        btnBack.setOnClickListener(v -> finish());

        btnLoginProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            startActivity(intent);
        });

        btnSignupProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, SignupActivity.class);
            startActivity(intent);
        });

        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        loadUserProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void loadUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user == null) {
            showGuestUI();
            return;
        }

        showLoggedInUI(user);
    }

    private void showGuestUI() {
        tvName.setText("Guest User");
        tvNameInfo.setText("Guest User");
        tvEmailInfo.setText("Not logged in");
        tvInitial.setText("?");

        btnLoginProfile.setVisibility(View.VISIBLE);
        btnSignupProfile.setVisibility(View.VISIBLE);
        btnLogout.setVisibility(View.GONE);
    }

    private void showLoggedInUI(FirebaseUser user) {
        btnLoginProfile.setVisibility(View.GONE);
        btnSignupProfile.setVisibility(View.GONE);
        btnLogout.setVisibility(View.VISIBLE);

        db.collection("Users")
                .document(user.getUid())
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    String username = "User";
                    String email = user.getEmail() != null ? user.getEmail() : "No email";

                    if (documentSnapshot.exists()) {
                        String firestoreUsername = documentSnapshot.getString("username");
                        String firestoreEmail = documentSnapshot.getString("email");

                        if (firestoreUsername != null && !firestoreUsername.trim().isEmpty()) {
                            username = firestoreUsername.trim();
                        }

                        if (firestoreEmail != null && !firestoreEmail.trim().isEmpty()) {
                            email = firestoreEmail.trim();
                        }
                    }

                    tvName.setText(username);
                    tvNameInfo.setText(username);
                    tvEmailInfo.setText(email);
                    tvInitial.setText(getInitials(username));
                })
                .addOnFailureListener(e -> {
                    String email = user.getEmail() != null ? user.getEmail() : "No email";

                    tvName.setText("User");
                    tvNameInfo.setText("User");
                    tvEmailInfo.setText(email);
                    tvInitial.setText("U");
                });
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

    private void showLogoutConfirmationDialog() {
        if (logoutDialog != null && logoutDialog.isShowing()) {
            return;
        }

        logoutDialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.dialog_logout_confirm, null);

        TextView tvDialogTitle = view.findViewById(R.id.tvDialogTitle);
        TextView tvDialogMessage = view.findViewById(R.id.tvDialogMessage);
        TextView btnCancelLogout = view.findViewById(R.id.btnCancelLogout);
        TextView btnConfirmLogout = view.findViewById(R.id.btnConfirmLogout);

        tvDialogTitle.setText("Logout?");
        tvDialogMessage.setText("Are you sure you want to logout from QuickChat?");
        btnConfirmLogout.setText("Logout");

        btnCancelLogout.setOnClickListener(v -> logoutDialog.dismiss());

        btnConfirmLogout.setOnClickListener(v -> {
            logoutDialog.dismiss();
            performLogout();
        });

        logoutDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        logoutDialog.setContentView(view);
        logoutDialog.setCancelable(true);

        if (logoutDialog.getWindow() != null) {
            logoutDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        logoutDialog.show();

        if (logoutDialog.getWindow() != null) {
            logoutDialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void performLogout() {
        setLogoutLoading(true);
        showLoadingDialog("Logging out...");

        btnLogout.postDelayed(() -> {
            mAuth.signOut();

            hideLoadingDialog();
            setLogoutLoading(false);

            Toast.makeText(ProfileActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 900);
    }

    private void setLogoutLoading(boolean isLoading) {
        btnLogout.setEnabled(!isLoading);
        btnLoginProfile.setEnabled(!isLoading);
        btnSignupProfile.setEnabled(!isLoading);
        btnBack.setEnabled(!isLoading);
        btnLogout.setText(isLoading ? "Please wait..." : "Logout");
    }

    private void showLoadingDialog(String message) {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            return;
        }

        loadingDialog = new Dialog(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_auth_loader, null);

        TextView tvLoaderText = view.findViewById(R.id.tvLoaderText);
        tvLoaderText.setText(message);

        loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        loadingDialog.setContentView(view);
        loadingDialog.setCancelable(false);

        if (loadingDialog.getWindow() != null) {
            loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        loadingDialog.show();

        if (loadingDialog.getWindow() != null) {
            loadingDialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.78),
                    android.view.ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void hideLoadingDialog() {
        if (loadingDialog != null) {
            if (loadingDialog.isShowing()) {
                loadingDialog.dismiss();
            }
            loadingDialog = null;
        }
    }

    @Override
    protected void onDestroy() {
        hideLoadingDialog();

        if (logoutDialog != null && logoutDialog.isShowing()) {
            logoutDialog.dismiss();
        }

        super.onDestroy();
    }
}