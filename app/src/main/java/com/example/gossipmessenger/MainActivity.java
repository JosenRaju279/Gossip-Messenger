package com.example.gossipmessenger;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;  // Firebase Authentication instance
    RecyclerView mainuserrecyclerview;  // RecyclerView for displaying users
    UserAdpter adapter;  // Adapter for RecyclerView
    FirebaseDatabase database;  // Firebase Database instance
    ArrayList<Users> userArrayList;  // ArrayList to hold users
    ImageView imglogout;  // ImageView for logout button
    ImageView cambut, setbut;  // ImageView for camera and settings buttons

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase instances
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize UI components
        cambut = findViewById(R.id.camBut);  // Camera button
        setbut = findViewById(R.id.settingbut);  // Settings button
        imglogout = findViewById(R.id.logoutimg);  // Logout button

        // Database reference to fetch user data
        DatabaseReference reference = database.getReference().child("user");

        // Initialize the ArrayList
        userArrayList = new ArrayList<>();

        // Fetch data from Firebase
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userArrayList.clear();  // Clear previous data before adding new ones
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Users users = dataSnapshot.getValue(Users.class);  // Convert snapshot to Users object
                    if (users != null) {
                        userArrayList.add(users);  // Add the user to the list
                    }
                }
                adapter.notifyDataSetChanged();  // Notify the adapter that data has changed
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error if needed
            }
        });

        // Handle logout button click
        imglogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a dialog asking the user to confirm logout
                Dialog dialog = new Dialog(MainActivity.this, R.style.dailoge);
                dialog.setContentView(R.layout.dailog_layout);
                Button no = dialog.findViewById(R.id.Nobutton);  // "No" button
                Button yes = dialog.findViewById(R.id.Yesbutton);  // "Yes" button

                // If "Yes" is clicked, sign out and redirect to login screen
                yes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseAuth.getInstance().signOut();  // Sign out from Firebase
                        Intent intent = new Intent(MainActivity.this, Login.class);  // Redirect to Login activity
                        startActivity(intent);
                    }
                });

                // If "No" is clicked, dismiss the dialog
                no.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });

                // Show the dialog
                dialog.show();
            }
        });

        // Handle settings button click
        setbut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to the Settings activity
                Intent intent = new Intent(MainActivity.this, Setting.class);
                startActivity(intent);
            }
        });

        // Handle camera button click
        cambut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open the camera to take a picture
                Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(intent, 10);  // Start the camera activity
            }
        });

        // Initialize RecyclerView and adapter
        mainuserrecyclerview = findViewById(R.id.mainuserrecyclerview);
        mainuserrecyclerview.setLayoutManager(new LinearLayoutManager(this));  // Set a LinearLayoutManager for RecyclerView
        adapter = new UserAdpter(MainActivity.this, userArrayList);  // Initialize the adapter
        mainuserrecyclerview.setAdapter(adapter);  // Set the adapter to RecyclerView

        // If the user is not authenticated, redirect to the Login activity
        if (auth.getCurrentUser() == null) {
            Intent intent = new Intent(MainActivity.this, Login.class);
            startActivity(intent);
        }
    }
}
