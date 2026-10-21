package com.example.gossipmessenger;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.lang.ref.WeakReference;

public class Login extends AppCompatActivity {
    TextView Logsignup;  // TextView for redirecting to the registration screen
    Button button;       // Button for login action
    EditText email, password;  // EditText for entering email and password
    FirebaseAuth auth;    // Firebase Authentication instance
    String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";  // Regex pattern for validating email format

    private ProgressDialog progressDialog; // ProgressDialog to show loading during login
    private WeakReference<Login> loginActivityWeakRef; // WeakReference to the Login activity to prevent memory leaks

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize the WeakReference to prevent memory leaks
        loginActivityWeakRef = new WeakReference<>(this);

        // Initialize progressDialog for loading indication
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Please Wait...");
        progressDialog.setCancelable(false); // Disable canceling the dialog by tapping outside

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance();
        button = findViewById(R.id.Logbutton); // Login button
        email = findViewById(R.id.editTextLogEmail); // Email input field
        password = findViewById(R.id.editTextLogPassword); // Password input field
        Logsignup = findViewById(R.id.loginsignup); // TextView for signup redirection

        // Set up click listener for signup redirect
        Logsignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Redirect to the Registration activity
                Intent intent = new Intent(Login.this, Registration.class);
                startActivity(intent);
                finish(); // Close the current activity (Login)
            }
        });

        // Set up click listener for login button
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String Email = email.getText().toString(); // Get the email entered by the user
                String pass = password.getText().toString(); // Get the password entered by the user

                // Check if email is empty
                if (TextUtils.isEmpty(Email)) {
                    Toast.makeText(Login.this, "Enter your Email to verify", Toast.LENGTH_SHORT).show();
                }
                // Check if password is empty
                else if (TextUtils.isEmpty(pass)) {
                    Toast.makeText(Login.this, "Enter your Password to verify", Toast.LENGTH_SHORT).show();
                }
                // Validate the email format using the regular expression
                else if (!Email.matches(emailPattern)) {
                    email.setError("Enter a Valid Email"); // Show error if email is invalid
                }
                // Validate that password is at least 6 characters long
                else if (pass.length() < 6) {
                    password.setError("Write More Than Six Characters");
                    Toast.makeText(Login.this, "Enter a Password with more than 6 Characters", Toast.LENGTH_SHORT).show();
                } else {
                    Login loginActivity = loginActivityWeakRef.get(); // Get the Login activity reference

                    // Check if the activity is not finishing (i.e., it's still in use)
                    if (loginActivity != null && !loginActivity.isFinishing()) {
                        progressDialog.show(); // Show the loading dialog
                    }

                    // Attempt to sign in the user with email and password
                    auth.signInWithEmailAndPassword(Email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Login loginActivity = loginActivityWeakRef.get(); // Get the Login activity reference

                            // Check if the activity is still valid and not finishing
                            if (loginActivity != null && !loginActivity.isFinishing()) {
                                if (progressDialog != null && progressDialog.isShowing()) {
                                    progressDialog.dismiss(); // Dismiss the progress dialog
                                }
                                if (task.isSuccessful()) {
                                    try {
                                        // If login is successful, move to the MainActivity
                                        Intent intent = new Intent(Login.this, MainActivity.class);
                                        startActivity(intent);
                                        finish(); // Close the Login activity
                                    } catch (Exception e) {
                                        // Show an error message if there's an exception
                                        Toast.makeText(Login.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                } else {
                                    // Show an error message if login fails
                                    Toast.makeText(Login.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
            progressDialog = null;
        }
        loginActivityWeakRef.clear();
    }
}
