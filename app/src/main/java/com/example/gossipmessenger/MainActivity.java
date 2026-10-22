package com.example.gossipmessenger;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private RecyclerView mainuserrecyclerview;
    private UserAdpter adapter;
    private ArrayList<Users> userArrayList;
    private ImageView imglogout, cambut, setbut;
    private ValueEventListener usersListener;
    private DatabaseReference usersReference;

    // Camera launcher
    private final ActivityResultLauncher<Void> cameraLauncher =
            registerForActivityResult(new ActivityResultContracts.TakePicturePreview(), bitmap -> {
                if (bitmap != null) {
                    Toast.makeText(MainActivity.this, "Photo captured!", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 1. First verify authentication
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Intent intent = new Intent(MainActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        // 2. Initialize UI components
        cambut = findViewById(R.id.camBut);
        setbut = findViewById(R.id.settingbut);
        imglogout = findViewById(R.id.logoutimg);
        mainuserrecyclerview = findViewById(R.id.mainuserrecyclerview);

        // 3. Initialize RecyclerView and Adapter BEFORE attaching Firebase listener to prevent NPE
        userArrayList = new ArrayList<>();
        mainuserrecyclerview.setLayoutManager(new LinearLayoutManager(this));
        adapter = new UserAdpter(MainActivity.this, userArrayList);
        mainuserrecyclerview.setAdapter(adapter);

        // 4. Initialize Firebase Database listener
        database = FirebaseDatabase.getInstance();
        usersReference = database.getReference().child("user");

        String currentUserId = currentUser.getUid();
        usersListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users user = dataSnapshot.getValue(Users.class);
                    if (user != null) {
                        // Ensure userId is populated
                        if (user.getUserId() == null || user.getUserId().isEmpty()) {
                            user.setUserId(dataSnapshot.getKey());
                        }
                        // Don't show the currently logged-in user in their own chat list
                        if (!currentUserId.equals(user.getUserId())) {
                            userArrayList.add(user);
                        }
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load users: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        };
        usersReference.addValueEventListener(usersListener);

        // 5. Setup Action Handlers
        // Logout Dialog
        imglogout.setOnClickListener(v -> showLogoutDialog());

        // Settings Button
        setbut.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Setting.class);
            startActivity(intent);
        });

        // Camera Button
        cambut.setOnClickListener(v -> {
            try {
                cameraLauncher.launch(null);
            } catch (Exception e) {
                Toast.makeText(MainActivity.this, "Camera not available: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLogoutDialog() {
        Dialog dialog = new Dialog(MainActivity.this, R.style.dailoge);
        dialog.setContentView(R.layout.dailog_layout);
        Button no = dialog.findViewById(R.id.Nobutton);
        Button yes = dialog.findViewById(R.id.Yesbutton);

        yes.setOnClickListener(v -> {
            dialog.dismiss();
            auth.signOut();
            Intent intent = new Intent(MainActivity.this, Login.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        no.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (usersReference != null && usersListener != null) {
            usersReference.removeEventListener(usersListener);
        }
    }
}
