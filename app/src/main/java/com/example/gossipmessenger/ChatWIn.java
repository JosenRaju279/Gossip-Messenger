package com.example.gossipmessenger;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatWIn extends AppCompatActivity {

    // Declare variables for receiver details and Firebase references
    String reviverimg, reciverUid, reciverName, SenderUID;
    CircleImageView profile;
    TextView reciverNname;
    CardView sendbtn;
    EditText writemsg;
    FirebaseAuth firebaseAuth;
    FirebaseDatabase database;
    public static String senderimg;
    public static String reciveriimg;
    String senderRoom, reciverRoom;
    RecyclerView messadapters;
    ArrayList<msgmodelClass> messagessArrayList;
    messageaAdpater mmessageadapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_win);

        // Initialize Firebase Auth and Database instances
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Set up RecyclerView for displaying messages
        messadapters = findViewById(R.id.messadapter);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true); // Scroll messages to the bottom
        messadapters.setLayoutManager(linearLayoutManager);

        // Initialize message list and adapter for RecyclerView
        messagessArrayList = new ArrayList<>();
        mmessageadapter = new messageaAdpater(ChatWIn.this, messagessArrayList);
        messadapters.setAdapter(mmessageadapter);

        // Retrieve receiver details from Intent
        reciverName = getIntent().getStringExtra("nameee");
        reviverimg = getIntent().getStringExtra("reciverimage");
        reciverUid = getIntent().getStringExtra("uid");

        // Check if received data is valid; show error if not
        if (reciverName == null || reviverimg == null || reciverUid == null) {
            Log.e("ChatWIn", "Received invalid intent extras");
            Toast.makeText(this, "Error loading chat", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize UI components for sending messages
        sendbtn = findViewById(R.id.sendbutton);
        writemsg = findViewById(R.id.writemessage);
        profile = findViewById(R.id.profilechat);
        reciverNname = findViewById(R.id.Recivername);

        // Load receiver's profile image using Picasso library
        if (reviverimg != null && !reviverimg.isEmpty()) {
            Picasso.get().load(reviverimg).into(profile);
        } else {
            // Load default profile image if the URL is invalid
            profile.setImageResource(R.drawable.man); // Ensure this image exists in res/drawable
        }

        // Set the receiver's name on the screen
        reciverNname.setText(reciverName);

        // Get the current user's UID (SenderUID) from Firebase Authentication
        SenderUID = firebaseAuth.getUid();
        if (SenderUID == null) {
            Log.e("ChatWIn", "Sender UID is null");
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Set up unique rooms for sender and receiver based on their UIDs
        senderRoom = SenderUID + reciverUid;
        reciverRoom = reciverUid + SenderUID;

        // Reference to the user's profile in the database
        DatabaseReference reference = database.getReference().child("users").child(SenderUID);
        DatabaseReference chatreference = database.getReference().child("chats").child(senderRoom).child("messages");

        // Retrieve existing messages for the chat
        chatreference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagessArrayList.clear(); // Clear old messages before displaying new ones
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    msgmodelClass messages = dataSnapshot.getValue(msgmodelClass.class); // Get message object
                    if (messages != null) {
                        messagessArrayList.add(messages); // Add message to the list
                    } else {
                        Log.w("ChatWIn", "Message is null, skipping.");
                    }
                }
                mmessageadapter.notifyDataSetChanged(); // Notify adapter to update UI
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatWIn", "Failed to read messages: " + error.getMessage());
            }
        });

        // Retrieve sender's profile image
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.child("profilepic").getValue() != null) {
                    senderimg = snapshot.child("profilepic").getValue().toString(); // Get sender's profile image URL
                    reciveriimg = reviverimg; // Set receiver's image URL
                } else {
                    Log.w("ChatWIn", "Profile picture not found for sender.");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("ChatWIn", "Failed to retrieve sender's profile picture: " + error.getMessage());
            }
        });

        // Send message logic when send button is clickedv
        sendbtn.setOnClickListener(v -> {
            String message = writemsg.getText().toString(); // Get the message input from user
            if (message.isEmpty()) {
                Toast.makeText(ChatWIn.this, "Enter the Message to send", Toast.LENGTH_SHORT).show();
                return;
            }
            writemsg.setText(""); // Clear message input field
            Date date = new Date(); // Get current date and time

            // Create a message object with sender UID, message text, and timestamp
            msgmodelClass messagess = new msgmodelClass(SenderUID, message, date.getTime());

            // Ensure Firebase Database instance is not null
            if (database == null) {
                Log.e("Firebase", "FirebaseDatabase instance is null");
                return;
            }

            // Send message to both sender's and receiver's chat rooms in Firebase
            database.getReference().child("chats").child(senderRoom).child("messages").push().setValue(messagess)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Send the same message to the receiver's chat room
                            database.getReference().child("chats").child(reciverRoom).child("messages").push().setValue(messagess);
                        } else {
                            Log.e("ChatWIn", "Failed to send message: " + task.getException());
                        }
                    });
        });
    }
}
