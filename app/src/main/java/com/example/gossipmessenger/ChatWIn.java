package com.example.gossipmessenger;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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

    private static final String TAG = "ChatWIn";

    public static String senderimg = "";
    public static String reciveriimg = "";

    private String reviverimg, reciverUid, reciverName, senderUID;
    private CircleImageView profile;
    private TextView receiverNameView;
    private CardView sendbtn;
    private EditText writemsg;
    private ImageView backButton;

    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase database;
    private String senderRoom, reciverRoom;
    private RecyclerView messadapters;
    private ArrayList<msgmodelClass> messagessArrayList;
    private messageaAdpater mmessageadapter;

    private DatabaseReference chatReference;
    private ValueEventListener chatListener;
    private DatabaseReference senderProfileRef;
    private ValueEventListener senderProfileListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_win);

        // Retrieve receiver details from Intent
        reciverName = getIntent().getStringExtra("nameee");
        reviverimg = getIntent().getStringExtra("reciverimage");
        reciverUid = getIntent().getStringExtra("uid");

        // Validate essential receiver data
        if (TextUtils.isEmpty(reciverUid)) {
            Log.e(TAG, "Receiver UID is missing");
            Toast.makeText(this, "Error loading chat: invalid user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (TextUtils.isEmpty(reciverName)) {
            reciverName = "User";
        }

        // Initialize Firebase
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        if (firebaseAuth.getCurrentUser() == null) {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        senderUID = firebaseAuth.getCurrentUser().getUid();

        // Set up unique rooms for 1-on-1 chat
        senderRoom = senderUID + reciverUid;
        reciverRoom = reciverUid + senderUID;

        // Cache receiver image URL for adapter
        reciveriimg = reviverimg != null ? reviverimg : "";

        // Bind UI components
        backButton = findViewById(R.id.backButton);
        sendbtn = findViewById(R.id.sendbutton);
        writemsg = findViewById(R.id.writemessage);
        profile = findViewById(R.id.profilechat);
        receiverNameView = findViewById(R.id.Recivername);
        messadapters = findViewById(R.id.messadapter);

        // Back button navigation
        if (backButton != null) {
            backButton.setOnClickListener(v -> finish());
        }

        // Set receiver name
        receiverNameView.setText(reciverName);

        // Load receiver profile image
        if (!TextUtils.isEmpty(reviverimg)) {
            Picasso.get()
                    .load(reviverimg)
                    .placeholder(R.drawable.man)
                    .error(R.drawable.man)
                    .into(profile);
        } else {
            profile.setImageResource(R.drawable.man);
        }

        // Setup RecyclerView
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        messadapters.setLayoutManager(linearLayoutManager);

        messagessArrayList = new ArrayList<>();
        mmessageadapter = new messageaAdpater(ChatWIn.this, messagessArrayList);
        messadapters.setAdapter(mmessageadapter);

        // Load sender profile picture from "user" node (fixed path mismatch from "users")
        senderProfileRef = database.getReference().child("user").child(senderUID);
        senderProfileListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists() && snapshot.child("profilepic").getValue() != null) {
                    senderimg = String.valueOf(snapshot.child("profilepic").getValue());
                    mmessageadapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "Failed to load sender profile picture: " + error.getMessage());
            }
        };
        senderProfileRef.addValueEventListener(senderProfileListener);

        // Listen for messages in this chat room
        chatReference = database.getReference().child("chats").child(senderRoom).child("messages");
        chatListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messagessArrayList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    msgmodelClass message = dataSnapshot.getValue(msgmodelClass.class);
                    if (message != null) {
                        messagessArrayList.add(message);
                    }
                }
                mmessageadapter.notifyDataSetChanged();
                if (!messagessArrayList.isEmpty()) {
                    messadapters.scrollToPosition(messagessArrayList.size() - 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to read messages: " + error.getMessage());
            }
        };
        chatReference.addValueEventListener(chatListener);

        // Send message handler
        sendbtn.setOnClickListener(v -> sendMessage());
    }

    private void sendMessage() {
        String messageText = writemsg.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return;
        }

        writemsg.setText("");
        long currentTime = new Date().getTime();

        msgmodelClass newMessage = new msgmodelClass(senderUID, messageText, currentTime);

        // Push to sender's room
        database.getReference().child("chats").child(senderRoom).child("messages")
                .push()
                .setValue(newMessage)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Push to receiver's room as well
                        database.getReference().child("chats").child(reciverRoom).child("messages")
                                .push()
                                .setValue(newMessage);
                    } else {
                        Log.e(TAG, "Failed to send message: " + task.getException());
                        Toast.makeText(ChatWIn.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (chatReference != null && chatListener != null) {
            chatReference.removeEventListener(chatListener);
        }
        if (senderProfileRef != null && senderProfileListener != null) {
            senderProfileRef.removeEventListener(senderProfileListener);
        }
    }
}
