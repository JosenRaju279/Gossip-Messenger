package com.example.gossipmessenger;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class splash extends AppCompatActivity {

    // Declare UI components
    ImageView logo;  // ImageView for the logo
    TextView name, own1, own2;  // TextViews for name and owners' names
    Animation UpAnima, DownAnima;  // Animations for the views

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash); // Ensure this layout exists

        // Initialize the UI components by finding them by their IDs
        logo = findViewById(R.id.logoimage);
        name = findViewById(R.id.logoname);
        own1 = findViewById(R.id.ower1);
        own2 = findViewById(R.id.ower2);

        // Load the animations from the res folder
        UpAnima = AnimationUtils.loadAnimation(this, R.anim.top_animation);
        DownAnima = AnimationUtils.loadAnimation(this, R.anim.down_animation);

        // Set the animations on the respective views
        logo.setAnimation(UpAnima);  // Apply upward animation to the logo
        name.setAnimation(UpAnima);  // Apply upward animation to the app name
        own1.setAnimation(DownAnima);  // Apply downward animation to the first owner name
        own2.setAnimation(DownAnima);  // Apply downward animation to the second owner name

        // Delay for 4 seconds before transitioning to the login screen
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                // After 4 seconds, navigate to the Login activity
                Intent intent = new Intent(splash.this, Login.class);
                startActivity(intent);
                finish();  // Close the splash screen activity
            }
        }, 4000);  // 4000 milliseconds delay (4 seconds)
    }
}
