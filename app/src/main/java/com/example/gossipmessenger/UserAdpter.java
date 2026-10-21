package com.example.gossipmessenger;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdpter extends RecyclerView.Adapter<UserAdpter.viewholder> {

    // Declare the activity and the list of users
    MainActivity mainActivity;
    ArrayList<Users> userArrayList;

    // Constructor for the adapter, receives the MainActivity and list of users
    public UserAdpter(MainActivity mainActivity, ArrayList<Users> userArrayList) {
        this.mainActivity = mainActivity;
        this.userArrayList = userArrayList;
    }

    // This method is called to inflate the view for each item in the list
    @NonNull
    @Override
    public viewholder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each user item
        View view = LayoutInflater.from(mainActivity).inflate(R.layout.user_item, parent, false);
        return new viewholder(view); // Return a new viewholder instance
    }

    // This method binds data to the views for each item
    @Override
    public void onBindViewHolder(@NonNull viewholder holder, int position) {
        Users users = userArrayList.get(position);  // Get the user at the current position

        // Set the user name and status
        holder.Username.setText(users.getUserName());  // Set the user name
        holder.UserStatus.setText(users.getStatus());  // Set the user status

        String profilePicUrl = users.getProfilepic();  // Get the profile picture URL

        // Check if the profilePicUrl is valid and not empty
        if (profilePicUrl != null && !profilePicUrl.isEmpty()) {
            // Use Picasso to load the profile picture from the URL
            Picasso.get()
                    .load(profilePicUrl)
                    .error(R.drawable.man)  // Set default image if the URL is invalid or not found
                    .into(holder.userimg);  // Load the image into the ImageView
        } else {
            // If no profile picture exists, set the default image from drawable
            holder.userimg.setImageResource(R.drawable.man);  // Default image
        }

        // Set a click listener for the entire item
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // On item click, navigate to the ChatWIn activity
                Intent intent = new Intent(mainActivity, ChatWIn.class);
                intent.putExtra("nameee", users.getUserName());  // Pass the user name to the next activity
                intent.putExtra("reciverimage", users.getProfilepic()); // Pass the user's profile picture URL (may be null)
                intent.putExtra("uid", users.getUserId());  // Pass the user ID to identify the receiver
                mainActivity.startActivity(intent);  // Start the chat activity
            }
        });
    }

    // This method returns the total number of items in the list
    @Override
    public int getItemCount() {
        return userArrayList.size();  // Return the size of the list
    }

    // Viewholder class to hold the views for each item
    public class viewholder extends RecyclerView.ViewHolder {
        // Declare the views for user image, name, and status
        CircleImageView userimg;  // Image view for the user's profile picture
        TextView Username;        // Text view for the user's name
        TextView UserStatus;      // Text view for the user's status

        // Constructor to initialize the views
        public viewholder(@NonNull View itemView) {
            super(itemView);
            userimg = itemView.findViewById(R.id.userimg);  // Initialize the profile image view
            Username = itemView.findViewById(R.id.Username);  // Initialize the username text view
            UserStatus = itemView.findViewById(R.id.UserStatus);  // Initialize the user status text view
        }
    }
}
