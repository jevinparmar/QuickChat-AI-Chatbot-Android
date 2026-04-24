package com.example.quickchataichatbot;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SignupActivity extends AppCompatActivity {

    private EditText etSignupUsername;
    private EditText etSignupEmail;
    private EditText etSignupPhone;
    private EditText etSignupPassword;
    private Button btnSignup;
    private TextView txtGoToLogin;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etSignupUsername = findViewById(R.id.etSignupUsername);
        etSignupEmail = findViewById(R.id.etSignupEmail);
        etSignupPhone = findViewById(R.id.etSignupPhone);
        etSignupPassword = findViewById(R.id.etSignupPassword);
        btnSignup = findViewById(R.id.btnSignup);
        txtGoToLogin = findViewById(R.id.txtGoToLogin);

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            Intent intent = new Intent(SignupActivity.this, ChatApp.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        btnSignup.setOnClickListener(v -> signupUser());

        txtGoToLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void signupUser() {
        String username = etSignupUsername.getText().toString().trim();
        String email = etSignupEmail.getText().toString().trim();
        String phone = etSignupPhone.getText().toString().trim();
        String password = etSignupPassword.getText().toString().trim();

        if (!validateSignupInputs(username, email, phone, password)) {
            return;
        }

        setSignupLoading(true);
        showLoadingDialog("Creating account...");

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        FirebaseUser firebaseUser = mAuth.getCurrentUser();

                        if (firebaseUser == null) {
                            hideLoadingDialog();
                            setSignupLoading(false);
                            Toast.makeText(SignupActivity.this, "User creation failed", Toast.LENGTH_LONG).show();
                            return;
                        }

                        String uid = firebaseUser.getUid();

                        Map<String, Object> userMap = new HashMap<>();
                        userMap.put("username", username);
                        userMap.put("email", email);
                        userMap.put("phone", phone);
                        userMap.put("createdAt", Timestamp.now());

                        db.collection("Users")
                                .document(uid)
                                .set(userMap)
                                .addOnSuccessListener(unused -> {
                                    hideLoadingDialog();
                                    setSignupLoading(false);

                                    Toast.makeText(SignupActivity.this, "Signup successful. Please login.", Toast.LENGTH_SHORT).show();

                                    mAuth.signOut();

                                    Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                                    intent.putExtra("signup_email", email);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    hideLoadingDialog();
                                    setSignupLoading(false);

                                    firebaseUser.delete();

                                    Toast.makeText(
                                            SignupActivity.this,
                                            "Failed to save user data: " + e.getMessage(),
                                            Toast.LENGTH_LONG
                                    ).show();
                                });

                    } else {
                        hideLoadingDialog();
                        setSignupLoading(false);

                        String errorMessage = "Signup failed";
                        if (authTask.getException() != null && authTask.getException().getMessage() != null) {
                            errorMessage = authTask.getException().getMessage();
                        }

                        Toast.makeText(SignupActivity.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private boolean validateSignupInputs(String username, String email, String phone, String password) {
        if (TextUtils.isEmpty(username)) {
            etSignupUsername.setError("Enter username");
            etSignupUsername.requestFocus();
            return false;
        }

        if (username.length() < 3) {
            etSignupUsername.setError("Username must be at least 3 characters");
            etSignupUsername.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            etSignupEmail.setError("Enter email");
            etSignupEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etSignupEmail.setError("Enter valid email");
            etSignupEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(phone)) {
            etSignupPhone.setError("Enter phone number");
            etSignupPhone.requestFocus();
            return false;
        }

        if (!Patterns.PHONE.matcher(phone).matches() || phone.length() < 10) {
            etSignupPhone.setError("Enter valid phone number");
            etSignupPhone.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etSignupPassword.setError("Enter password");
            etSignupPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etSignupPassword.setError("Password must be at least 6 characters");
            etSignupPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void setSignupLoading(boolean isLoading) {
        btnSignup.setEnabled(!isLoading);
        etSignupUsername.setEnabled(!isLoading);
        etSignupEmail.setEnabled(!isLoading);
        etSignupPhone.setEnabled(!isLoading);
        etSignupPassword.setEnabled(!isLoading);
        txtGoToLogin.setEnabled(!isLoading);
        btnSignup.setText(isLoading ? "Please wait..." : "Sign Up");
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
        super.onDestroy();
    }
}