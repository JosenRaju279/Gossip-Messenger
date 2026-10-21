package com.example.gossipmessenger;

import static com.example.gossipmessenger.ChatWIn.reciveriimg;
import static com.example.gossipmessenger.ChatWIn.senderimg;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class messageaAdpater extends RecyclerView.Adapter {

    Context context;
    ArrayList<msgmodelClass> messagesAdpaterArrayList;  // ArrayList to hold message objects
    int ITEM_SEND = 1;  // Constant for sent messages
    int ITEM_RECIVER = 2;  // Constant for received messages

    // Constructor to initialize context and message list
    public messageaAdpater(Context context, ArrayList<msgmodelClass> messagesAdpaterArrayList) {
        this.context = context;
        this.messagesAdpaterArrayList = messagesAdpaterArrayList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate different layouts based on the message type (sent or received)
        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false);
            return new senderViewHolder(view);  // Return sender view holder for sent messages
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.reciver_layout, parent, false);
            return new reciverViewHolder(view);  // Return receiver view holder for received messages
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        msgmodelClass messages = messagesAdpaterArrayList.get(position);  // Get message at current position

        // If the message is sent by the current user
        if (holder.getClass() == senderViewHolder.class) {
            senderViewHolder viewHolder = (senderViewHolder) holder;
            viewHolder.msgtext.setText(messages.getMessage());  // Set message text

            // Check if sender image is available and load it using Picasso, else use default image
            if (senderimg != null && !senderimg.isEmpty()) {
                Picasso.get().load(senderimg).into(viewHolder.circleImageView);
            } else {
                viewHolder.circleImageView.setImageResource(R.drawable.man);  // Default image
            }

        } else {
            // If the message is received
            reciverViewHolder viewHolder = (reciverViewHolder) holder;
            viewHolder.msgtext.setText(messages.getMessage());  // Set message text

            // Check if receiver image is available and load it using Picasso, else use default image
            if (reciveriimg != null && !reciveriimg.isEmpty()) {
                Picasso.get().load(reciveriimg).into(viewHolder.circleImageView);
            } else {
                viewHolder.circleImageView.setImageResource(R.drawable.man);  // Default image
            }
        }
    }

    @Override
    public int getItemCount() {
        return messagesAdpaterArrayList.size();  // Return total number of messages in the list
    }

    @Override
    public int getItemViewType(int position) {
        msgmodelClass messages = messagesAdpaterArrayList.get(position);  // Get message at current position

        // Check if the current message sender is the logged-in user or the receiver
        if (FirebaseAuth.getInstance().getCurrentUser().getUid().equals(messages.getSenderid())) {
            return ITEM_SEND;  // Return ITEM_SEND for sent messages
        } else {
            return ITEM_RECIVER;  // Return ITEM_RECIVER for received messages
        }
    }

    // View holder for sent messages
    class senderViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;  // Profile image of the sender
        TextView msgtext;  // Message text of the sender

        public senderViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.profilerggg);  // Bind profile image view
            msgtext = itemView.findViewById(R.id.msgsendertyp);  // Bind message text view
        }
    }

    // View holder for received messages
    class reciverViewHolder extends RecyclerView.ViewHolder {

        CircleImageView circleImageView;  // Profile image of the receiver
        TextView msgtext;  // Message text of the receiver

        public reciverViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.pro);  // Bind profile image view
            msgtext = itemView.findViewById(R.id.recivertextset);  // Bind message text view
        }
    }
}
