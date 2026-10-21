package com.example.gossipmessenger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;

public class Setting extends AppCompatActivity {

    // Declare UI components
    ImageView setprofile;  // ImageView for profile picture
    EditText setname, setstatus;  // EditTexts for username and status
    Button donebut;  // Button to save changes
    FirebaseDatabase database;  // Firebase Realtime Database instance
    FirebaseAuth auth;  // Firebase Authentication instance
    FirebaseStorage storage;  // Firebase Storage instance
    String email, password;  // User email and password
    Uri setImageUri;  // URI for selected image

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();

        // Initialize UI components
        setprofile = findViewById(R.id.settingprofile);
        setname = findViewById(R.id.settingname);
        setstatus = findViewById(R.id.settingstatus);
        donebut = findViewById(R.id.donebut);

        // Reference to Firebase user node for current user
        DatabaseReference reference = database.getReference().child("user").child(auth.getUid());
        StorageReference storageReference = storage.getReference().child("upload").child(auth.getUid());

        // Fetch user data from Firebase and set to UI components
        reference.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                // Safely retrieve values from Firebase
                email = snapshot.child("mail").getValue(String.class);
                password = snapshot.child("password").getValue(String.class);
                String name = snapshot.child("userName").getValue(String.class);
                String profile = snapshot.child("profilepic").getValue(String.class);
                String status = snapshot.child("status").getValue(String.class);

                // Set name and status in respective EditTexts if available
                if (name != null && !name.isEmpty()) {
                    setname.setText(name);
                }
                if (status != null && !status.isEmpty()) {
                    setstatus.setText(status);
                }

                // Set the profile image, load default if no profile picture exists
                if (profile != null && !profile.isEmpty()) {
                    Picasso.get()
                            .load(profile)
                            .error(R.drawable.man)  // Default image in case of error
                            .into(setprofile);
                } else {
                    setprofile.setImageResource(R.drawable.man);  // Default profile image
                }
            }

            @Override
            public void onCancelled(@NonNull com.google.firebase.database.DatabaseError error) {
                Toast.makeText(Setting.this, "Failed to load user data", Toast.LENGTH_SHORT).show();
            }
        });

        // Handle profile image selection
        setprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent to select image from device storage
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), 10);
            }
        });

        // Handle save button click to update profile details
        donebut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = setname.getText().toString().trim();
                String status = setstatus.getText().toString().trim();

                // Validate the input fields
                if (name.isEmpty()) {
                    setname.setError("Name cannot be empty");
                    return;
                }
                if (status.isEmpty()) {
                    setstatus.setError("Status cannot be empty");
                    return;
                }

                // Reference to the current user's data in Firebase Realtime Database
                DatabaseReference reference = database.getReference().child("user").child(auth.getUid());

                // If a new profile image is selected, upload it
                if (setImageUri != null) {
                    storageReference.putFile(setImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                // Get the URL of the uploaded image
                                storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String finalImageUri = uri.toString();

                                        // Prepare the data for updating Firebase
                                        Map<String, Object> updates = new HashMap<>();
                                        updates.put("userName", name);
                                        updates.put("status", status);
                                        updates.put("profilepic", finalImageUri);

                                        // Update only the fields that have changed
                                        reference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    // Notify user and navigate back to main activity
                                                    Toast.makeText(Setting.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                                    startActivity(new Intent(Setting.this, MainActivity.class));
                                                    finish();
                                                } else {
                                                    Toast.makeText(Setting.this, "Something went wrong: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });
                            } else {
                                Toast.makeText(Setting.this, "Image upload failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    // No new image selected, update only name and status
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("userName", name);
                    updates.put("status", status);

                    reference.updateChildren(updates).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Notify user and navigate back to main activity
                                Toast.makeText(Setting.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(Setting.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(Setting.this, "Something went wrong: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
            }
        });
    }

    // Handle result of image selection
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10 && data != null) {
            // Get URI of the selected image
            setImageUri = data.getData();
            setprofile.setImageURI(setImageUri);  // Set the selected image to ImageView
        }
    }
}
