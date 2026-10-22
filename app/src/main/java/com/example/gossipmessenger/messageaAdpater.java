package com.example.gossipmessenger;

import static com.example.gossipmessenger.ChatWIn.reciveriimg;
import static com.example.gossipmessenger.ChatWIn.senderimg;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class messageaAdpater extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM_SEND = 1;
    private static final int ITEM_RECEIVER = 2;

    private final Context context;
    private final ArrayList<msgmodelClass> messagesList;
    private final SimpleDateFormat timeFormat;

    public messageaAdpater(Context context, ArrayList<msgmodelClass> messagesList) {
        this.context = context;
        this.messagesList = messagesList != null ? messagesList : new ArrayList<>();
        this.timeFormat = new SimpleDateFormat("hh:mm a", Locale.getDefault());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ITEM_SEND) {
            View view = LayoutInflater.from(context).inflate(R.layout.sender_layout, parent, false);
            return new SenderViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.reciver_layout, parent, false);
            return new ReceiverViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        msgmodelClass message = messagesList.get(position);
        if (message == null) {
            return;
        }

        String formattedTime = message.getTimeStamp() > 0 ? timeFormat.format(new Date(message.getTimeStamp())) : "";

        if (holder instanceof SenderViewHolder) {
            SenderViewHolder senderHolder = (SenderViewHolder) holder;
            senderHolder.msgText.setText(message.getMessage());

            if (!TextUtils.isEmpty(formattedTime)) {
                senderHolder.timestamp.setText(formattedTime);
                senderHolder.timestamp.setVisibility(View.VISIBLE);
            } else {
                senderHolder.timestamp.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(senderimg)) {
                Picasso.get()
                        .load(senderimg)
                        .placeholder(R.drawable.man)
                        .error(R.drawable.man)
                        .into(senderHolder.circleImageView);
            } else {
                senderHolder.circleImageView.setImageResource(R.drawable.man);
            }

        } else if (holder instanceof ReceiverViewHolder) {
            ReceiverViewHolder receiverHolder = (ReceiverViewHolder) holder;
            receiverHolder.msgText.setText(message.getMessage());

            if (!TextUtils.isEmpty(formattedTime)) {
                receiverHolder.timestamp.setText(formattedTime);
                receiverHolder.timestamp.setVisibility(View.VISIBLE);
            } else {
                receiverHolder.timestamp.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(reciveriimg)) {
                Picasso.get()
                        .load(reciveriimg)
                        .placeholder(R.drawable.man)
                        .error(R.drawable.man)
                        .into(receiverHolder.circleImageView);
            } else {
                receiverHolder.circleImageView.setImageResource(R.drawable.man);
            }
        }
    }

    @Override
    public int getItemCount() {
        return messagesList.size();
    }

    @Override
    public int getItemViewType(int position) {
        msgmodelClass message = messagesList.get(position);
        if (message == null) {
            return ITEM_RECEIVER;
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getUid().equals(message.getSenderid())) {
            return ITEM_SEND;
        } else {
            return ITEM_RECEIVER;
        }
    }

    static class SenderViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView msgText;
        TextView timestamp;

        public SenderViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.profilerggg);
            msgText = itemView.findViewById(R.id.msgsendertyp);
            timestamp = itemView.findViewById(R.id.sender_timestamp);
        }
    }

    static class ReceiverViewHolder extends RecyclerView.ViewHolder {
        CircleImageView circleImageView;
        TextView msgText;
        TextView timestamp;

        public ReceiverViewHolder(@NonNull View itemView) {
            super(itemView);
            circleImageView = itemView.findViewById(R.id.pro);
            msgText = itemView.findViewById(R.id.recivertextset);
            timestamp = itemView.findViewById(R.id.receiver_timestamp);
        }
    }
}
