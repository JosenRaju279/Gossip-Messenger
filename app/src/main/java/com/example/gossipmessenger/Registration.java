package com.example.gossipmessenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class Registration extends AppCompatActivity {

    // UI components
    TextView loginbut;
    EditText rg_username, rg_email, rg_password, rg_repassword;
    Button rg_signup;
    CircleImageView rg_profileImg;
    FirebaseAuth auth;
    Uri imageURI;
    String imageuri;
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    FirebaseDatabase database;
    FirebaseStorage storage;
    ProgressDialog progressDialog;
    String defaultImageUrl = "https://example.com/default_profile_image.jpg"; // Default image URL

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize the ProgressDialog to show while registering
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Establishing The Account");
        progressDialog.setCancelable(false);

        // Hide the action bar for a cleaner UI
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize Firebase components
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        auth = FirebaseAuth.getInstance();

        // Bind UI components with their respective views
        loginbut = findViewById(R.id.RgLogin);
        rg_username = findViewById(R.id.RgUsername);
        rg_email = findViewById(R.id.RgEmail);
        rg_password = findViewById(R.id.RgPassword);
        rg_repassword = findViewById(R.id.RgRePassword);
        rg_profileImg = findViewById(R.id.RgProfilepic);
        rg_signup = findViewById(R.id.RgSignup);

        // Redirect to the login screen when "Login" text is clicked
        loginbut.setOnClickListener(v -> {
            Intent intent = new Intent(Registration.this, Login.class);
            startActivity(intent);
            finish();
        });

        // Handle user registration when "Sign Up" button is clicked
        rg_signup.setOnClickListener(v -> {
            String name = rg_username.getText().toString();
            String email = rg_email.getText().toString();
            String password = rg_password.getText().toString();
            String confirmPassword = rg_repassword.getText().toString();
            String status = "Hey I'm Using This Application";

            // Validation checks for empty fields
            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(email) ||
                    TextUtils.isEmpty(password) || TextUtils.isEmpty(confirmPassword)) {
                Toast.makeText(Registration.this, "Please Enter Valid Information", Toast.LENGTH_SHORT).show();
            }
            // Check if the email format is valid
            else if (!email.matches(emailPattern)) {
                rg_email.setError("Type A Valid Email Here");
            }
            // Check if the password length is less than 6
            else if (password.length() < 6) {
                rg_password.setError("Password Must Be 6 Characters Or More");
            }
            // Check if passwords match
            else if (!password.equals(confirmPassword)) {
                rg_password.setError("The Password Doesn't Match");
            } else {
                progressDialog.show();
                // Create user with email and password using FirebaseAuth
                auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        String id = task.getResult().getUser().getUid();
                        DatabaseReference reference = database.getReference().child("user").child(id);
                        StorageReference storageReference = storage.getReference().child("Upload").child(id);

                        // If an image is selected, upload it to Firebase Storage
                        if (imageURI != null) {
                            storageReference.putFile(imageURI).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    // Once uploaded, get the image URL and store user data
                                    storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                        imageuri = uri.toString();
                                        Users user = new Users(imageuri, email, name, password, id, status);
                                        reference.setValue(user).addOnCompleteListener(task2 -> {
                                            progressDialog.dismiss();
                                            if (task2.isSuccessful()) {
                                                // Redirect to MainActivity after successful registration
                                                Intent intent = new Intent(Registration.this, MainActivity.class);
                                                startActivity(intent);
                                                finish();
                                            } else {
                                                Toast.makeText(Registration.this, "Error in creating the user", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    });
                                }
                            });
                        } else {
                            // If no image is selected, use the default image URL
                            Users user = new Users(defaultImageUrl, email, name, password, id, status);
                            reference.setValue(user).addOnCompleteListener(task2 -> {
                                progressDialog.dismiss();
                                if (task2.isSuccessful()) {
                                    // Redirect to MainActivity after successful registration
                                    Intent intent = new Intent(Registration.this, MainActivity.class);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(Registration.this, "Error in creating the user", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    } else {
                        progressDialog.dismiss();
                        // Handle email collision (email already in use)
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(Registration.this, "Email is already in use", Toast.LENGTH_SHORT).show();
                        } else {
                            // Handle other errors
                            Toast.makeText(Registration.this, "Error while creating. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        // Open image picker when the profile image is clicked
        rg_profileImg.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // If the request code is 10, then handle the selected image
        if (requestCode == 10 && data != null) {
            imageURI = data.getData();
            rg_profileImg.setImageURI(imageURI);
        }
        // If no image is selected, set the default image
        else if (requestCode == 10 && data == null) {
            imageURI = Uri.parse("android.resource://" + getPackageName() + "/" + R.drawable.man); // Default image in drawable
            rg_profileImg.setImageURI(imageURI);
        }
    }
}
