package com.example.gossipmessenger;

import android.app.Application;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class GossipApplication extends Application {

    private static final String TAG = "GossipApplication";

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            // Ensure Firebase is initialized
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
            }
            // Enable offline disk persistence for smooth performance
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        } catch (Exception e) {
            Log.w(TAG, "Firebase initialization warning: " + e.getMessage());
        }
    }
}
