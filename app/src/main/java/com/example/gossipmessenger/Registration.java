package com.example.gossipmessenger;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class Registration extends AppCompatActivity {

    private TextView loginRedirect, tapToChange;
    private TextInputLayout usernameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private TextInputEditText rgUsername, rgEmail, rgPassword, rgRePassword;
    private Button rgSignup;
    private CircleImageView rgProfileImg;

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private FirebaseStorage storage;
    private Uri selectedImageUri = null;
    private Dialog loadingDialog;
    private TextView loadingDialogText;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    selectedImageUri = uri;
                    rgProfileImg.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        try {
            auth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance();
            storage = FirebaseStorage.getInstance();
        } catch (Exception e) {
            Toast.makeText(this, "Firebase error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Bind views
        loginRedirect = findViewById(R.id.RgLogin);
        tapToChange = findViewById(R.id.tapToChange);
        usernameLayout = findViewById(R.id.usernameLayout);
        emailLayout = findViewById(R.id.emailRegLayout);
        passwordLayout = findViewById(R.id.passwordRegLayout);
        confirmPasswordLayout = findViewById(R.id.confirmPasswordLayout);
        rgUsername = findViewById(R.id.RgUsername);
        rgEmail = findViewById(R.id.RgEmail);
        rgPassword = findViewById(R.id.RgPassword);
        rgRePassword = findViewById(R.id.RgRePassword);
        rgProfileImg = findViewById(R.id.RgProfilepic);
        rgSignup = findViewById(R.id.RgSignup);

        setupLoadingDialog();

        // Click listeners for picking profile image
        View.OnClickListener pickImageListener = v -> imagePickerLauncher.launch("image/*");
        if (rgProfileImg != null) rgProfileImg.setOnClickListener(pickImageListener);
        if (tapToChange != null) tapToChange.setOnClickListener(pickImageListener);

        // Redirect to Login screen
        if (loginRedirect != null) {
            loginRedirect.setOnClickListener(v -> {
                Intent intent = new Intent(Registration.this, Login.class);
                startActivity(intent);
                finish();
            });
        }

        // Signup button click listener
        if (rgSignup != null) {
            rgSignup.setOnClickListener(v -> handleRegistration());
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

    private void handleRegistration() {
        String name = rgUsername.getText() != null ? rgUsername.getText().toString().trim() : "";
        String email = rgEmail.getText() != null ? rgEmail.getText().toString().trim() : "";
        String password = rgPassword.getText() != null ? rgPassword.getText().toString().trim() : "";
        String confirmPassword = rgRePassword.getText() != null ? rgRePassword.getText().toString().trim() : "";
        String status = "Hey I'm Using Gossip Messenger";

        usernameLayout.setError(null);
        emailLayout.setError(null);
        passwordLayout.setError(null);
        confirmPasswordLayout.setError(null);

        if (TextUtils.isEmpty(name)) {
            usernameLayout.setError("Username is required");
            rgUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailLayout.setError("Email is required");
            rgEmail.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailLayout.setError("Enter a valid email address");
            rgEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordLayout.setError("Password is required");
            rgPassword.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordLayout.setError("Password must be at least 6 characters");
            rgPassword.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordLayout.setError("Passwords do not match");
            rgRePassword.requestFocus();
            return;
        }

        if (auth == null || database == null) {
            Toast.makeText(this, "Firebase services unavailable", Toast.LENGTH_SHORT).show();
            return;
        }

        showLoading("Creating account...");

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                hideLoading();
                if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                    emailLayout.setError("This email is already registered");
                } else {
                    String msg = task.getException() != null ? task.getException().getMessage() : "Registration failed";
                    Toast.makeText(Registration.this, msg, Toast.LENGTH_LONG).show();
                }
                return;
            }

            if (auth.getCurrentUser() == null) {
                hideLoading();
                Toast.makeText(Registration.this, "Authentication error", Toast.LENGTH_SHORT).show();
                return;
            }

            String userId = auth.getCurrentUser().getUid();
            DatabaseReference userRef = database.getReference().child("user").child(userId);
            StorageReference storageRef = storage != null ? storage.getReference().child("Upload").child(userId) : null;

            if (selectedImageUri != null && storageRef != null) {
                storageRef.putFile(selectedImageUri).addOnCompleteListener(uploadTask -> {
                    if (uploadTask.isSuccessful()) {
                        storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            saveUserToDatabase(userRef, userId, name, email, uri.toString(), status);
                        }).addOnFailureListener(e -> {
                            saveUserToDatabase(userRef, userId, name, email, "", status);
                        });
                    } else {
                        saveUserToDatabase(userRef, userId, name, email, "", status);
                    }
                });
            } else {
                saveUserToDatabase(userRef, userId, name, email, "", status);
            }
        });
    }

    private void saveUserToDatabase(DatabaseReference userRef, String userId, String name, String email, String profilePic, String status) {
        Users newUser = new Users(userId, name, email, profilePic, status);

        userRef.setValue(newUser).addOnCompleteListener(task -> {
            hideLoading();
            if (task.isSuccessful()) {
                Toast.makeText(Registration.this, "Account created successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(Registration.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Failed to save profile";
                Toast.makeText(Registration.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        hideLoading();
        loadingDialog = null;
    }
}
