package com.example.gossipmessenger;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class Setting extends AppCompatActivity {

    private ImageView setprofile, backButton;
    private TextView tapToChange;
    private TextInputLayout nameLayout, statusLayout;
    private TextInputEditText setname, setstatus;
    private Button donebut;

    private FirebaseDatabase database;
    private FirebaseAuth auth;
    private FirebaseStorage storage;
    private Uri setImageUri = null;
    private String currentProfilePicUrl = "";
    private Dialog loadingDialog;
    private TextView loadingDialogText;

    private final ActivityResultLauncher<String> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    setImageUri = uri;
                    setprofile.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        try {
            auth = FirebaseAuth.getInstance();
            database = FirebaseDatabase.getInstance();
            storage = FirebaseStorage.getInstance();
        } catch (Exception e) {
            Toast.makeText(this, "Firebase error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        FirebaseUser currentUser = auth != null ? auth.getCurrentUser() : null;
        if (currentUser == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userId = currentUser.getUid();

        // Bind UI components
        backButton = findViewById(R.id.settingBackButton);
        setprofile = findViewById(R.id.settingprofile);
        tapToChange = findViewById(R.id.tapToChangeProfile);
        nameLayout = findViewById(R.id.nameInputLayout);
        statusLayout = findViewById(R.id.statusInputLayout);
        setname = findViewById(R.id.settingname);
        setstatus = findViewById(R.id.settingstatus);
        donebut = findViewById(R.id.donebut);

        setupLoadingDialog();

        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        View.OnClickListener pickListener = v -> imagePickerLauncher.launch("image/*");
        if (setprofile != null) setprofile.setOnClickListener(pickListener);
        if (tapToChange != null) tapToChange.setOnClickListener(pickListener);

        DatabaseReference userReference = database.getReference().child("user").child(userId);
        showLoading("Loading profile...");

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hideLoading();
                if (snapshot.exists()) {
                    String name = snapshot.child("userName").getValue(String.class);
                    String status = snapshot.child("status").getValue(String.class);
                    currentProfilePicUrl = snapshot.child("profilepic").getValue(String.class);

                    if (!TextUtils.isEmpty(name)) {
                        setname.setText(name);
                    }
                    if (!TextUtils.isEmpty(status)) {
                        setstatus.setText(status);
                    }

                    if (!TextUtils.isEmpty(currentProfilePicUrl)) {
                        Picasso.get()
                                .load(currentProfilePicUrl)
                                .placeholder(R.drawable.man)
                                .error(R.drawable.man)
                                .into(setprofile);
                    } else {
                        setprofile.setImageResource(R.drawable.man);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                hideLoading();
                Toast.makeText(Setting.this, "Failed to load profile", Toast.LENGTH_SHORT).show();
            }
        });

        if (donebut != null) {
            donebut.setOnClickListener(v -> saveProfileChanges(userId, userReference));
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

    private void saveProfileChanges(String userId, DatabaseReference userReference) {
        String name = setname.getText() != null ? setname.getText().toString().trim() : "";
        String status = setstatus.getText() != null ? setstatus.getText().toString().trim() : "";

        nameLayout.setError(null);
        statusLayout.setError(null);

        if (TextUtils.isEmpty(name)) {
            nameLayout.setError("Name cannot be empty");
            setname.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(status)) {
            statusLayout.setError("Status cannot be empty");
            setstatus.requestFocus();
            return;
        }

        showLoading("Saving changes...");

        if (setImageUri != null && storage != null) {
            StorageReference storageRef = storage.getReference().child("Upload").child(userId);
            storageRef.putFile(setImageUri).addOnCompleteListener(uploadTask -> {
                if (uploadTask.isSuccessful()) {
                    storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("userName", name);
                        updates.put("status", status);
                        updates.put("profilepic", uri.toString());
                        updateFirebaseProfile(userReference, updates);
                    }).addOnFailureListener(e -> {
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("userName", name);
                        updates.put("status", status);
                        updateFirebaseProfile(userReference, updates);
                    });
                } else {
                    hideLoading();
                    Toast.makeText(Setting.this, "Image upload failed, keeping current picture", Toast.LENGTH_SHORT).show();
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("userName", name);
                    updates.put("status", status);
                    updateFirebaseProfile(userReference, updates);
                }
            });
        } else {
            Map<String, Object> updates = new HashMap<>();
            updates.put("userName", name);
            updates.put("status", status);
            updateFirebaseProfile(userReference, updates);
        }
    }

    private void updateFirebaseProfile(DatabaseReference userReference, Map<String, Object> updates) {
        userReference.updateChildren(updates).addOnCompleteListener(task -> {
            hideLoading();
            if (task.isSuccessful()) {
                Toast.makeText(Setting.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                String error = task.getException() != null ? task.getException().getMessage() : "Failed to update profile";
                Toast.makeText(Setting.this, error, Toast.LENGTH_SHORT).show();
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
