package com.example.gossipmessenger;

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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class Login extends AppCompatActivity {

    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText editTextEmail, editTextPassword;
    private Button loginButton;
    private TextView signupRedirect, forgotPassword;
    private FirebaseAuth auth;
    private Dialog loadingDialog;
    private TextView loadingDialogText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Safe Firebase Auth initialization
        try {
            auth = FirebaseAuth.getInstance();
        } catch (Exception e) {
            Toast.makeText(this, "Firebase error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Bind UI components
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        editTextEmail = findViewById(R.id.editTextLogEmail);
        editTextPassword = findViewById(R.id.editTextLogPassword);
        loginButton = findViewById(R.id.Logbutton);
        signupRedirect = findViewById(R.id.loginsignup);
        forgotPassword = findViewById(R.id.forgotPassword);

        // Setup loading dialog using dedicated dialog_loading layout (no ClassCastException)
        setupLoadingDialog();

        // Redirect to Registration screen
        if (signupRedirect != null) {
            signupRedirect.setOnClickListener(v -> {
                Intent intent = new Intent(Login.this, Registration.class);
                startActivity(intent);
            });
        }

        // Forgot password flow
        if (forgotPassword != null) {
            forgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        }

        // Login button click handler
        if (loginButton != null) {
            loginButton.setOnClickListener(v -> handleLogin());
        }
    }

    private void setupLoadingDialog() {
        try {
            loadingDialog = new Dialog(this);
            loadingDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_loading, null);
            loadingDialog.setContentView(dialogView);
            if (loadingDialog.getWindow() != null) {
                loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            }
            loadingDialog.setCancelable(false);
            loadingDialogText = dialogView.findViewById(R.id.dialogLoadingText);
        } catch (Exception e) {
            loadingDialog = null;
        }
    }

    private void showLoading(String message) {
        if (!isFinishing() && !isDestroyed()) {
            if (loadingDialog != null) {
                if (loadingDialogText != null && message != null) {
                    loadingDialogText.setText(message);
                }
                if (!loadingDialog.isShowing()) {
                    loadingDialog.show();
                }
            }
        }
    }

    private void hideLoading() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            try {
                loadingDialog.dismiss();
            } catch (Exception ignored) {}
        }
    }

    private void handleLogin() {
        String email = editTextEmail.getText() != null ? editTextEmail.getText().toString().trim() : "";
        String pass = editTextPassword.getText() != null ? editTextPassword.getText().toString().trim() : "";

        // Reset errors
        emailLayout.setError(null);
        passwordLayout.setError(null);

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            editTextEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            editTextEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(pass)) {
            passwordLayout.setError("Password is required");
            editTextPassword.requestFocus();
            return;
        }

        if (pass.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            editTextPassword.requestFocus();
            return;
        }

        if (auth == null) {
            Toast.makeText(this, "Authentication service unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading("Signing in...");

        auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(task -> {
            hideLoading();
            if (task.isSuccessful()) {
                Intent intent = new Intent(Login.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Exception exception = task.getException();
                if (exception instanceof FirebaseAuthInvalidUserException) {
                    emailLayout.setError("No account found with this email");
                } else if (exception instanceof FirebaseAuthInvalidCredentialsException) {
                    passwordLayout.setError("Invalid password");
                } else {
                    String msg = exception != null ? exception.getMessage() : "Authentication failed";
                    Toast.makeText(Login.this, msg, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");
        builder.setMessage("Enter your registered email address to receive a password reset link.");

        final EditText input = new EditText(this);
        input.setHint("Email address");
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        input.setPadding(48, 24, 48, 24);

        String currentEmail = editTextEmail.getText() != null ? editTextEmail.getText().toString().trim() : "";
        if (!TextUtils.isEmpty(currentEmail)) {
            input.setText(currentEmail);
        }

        builder.setView(input);

        builder.setPositiveButton("Send", (dialog, which) -> {
            String resetEmail = input.getText().toString().trim();
            if (TextUtils.isEmpty(resetEmail) || !Patterns.EMAIL_ADDRESS.matcher(resetEmail).matches()) {
                Toast.makeText(Login.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
                return;
            }

            if (auth != null) {
                auth.sendPasswordResetEmail(resetEmail).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(Login.this, "Password reset email sent!", Toast.LENGTH_LONG).show();
                    } else {
                        String error = task.getException() != null ? task.getException().getMessage() : "Failed to send reset email";
                        Toast.makeText(Login.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoading();
        loadingDialog = null;
    }
}
