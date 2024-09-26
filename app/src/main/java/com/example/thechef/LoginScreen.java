package com.example.thechef;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class LoginScreen extends AppCompatActivity {
    EditText password, email;
    FirebaseAuth mAuth;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_screen);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Initialize UI components
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        progressBar = findViewById(R.id.progressBar);  // Make sure you have a progress bar in your layout

        // Navigate to RegisterScreen when "su" is clicked
        TextView su = findViewById(R.id.signup);
        su.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginScreen.this, RegisterScreen.class));
            }
        });

        // Handle login button click
        Button th = findViewById(R.id.toHome);
        th.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                 // Show progress bar

                String emailV = email.getText().toString().trim();
                String passwordV = password.getText().toString().trim();

                if (TextUtils.isEmpty(emailV)) {
                    Toast.makeText(LoginScreen.this, "Please Enter Email", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);  // Hide progress bar
                    return;
                }
                if (TextUtils.isEmpty(passwordV)) {
                    Toast.makeText(LoginScreen.this, "Please Enter Password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);  // Hide progress bar
                    return;
                }

                // Sign in with Firebase Authentication
                mAuth.signInWithEmailAndPassword(emailV, passwordV)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {

                                progressBar.setVisibility(View.VISIBLE); // Hide progress bar
                                if (task.isSuccessful()) {
                                    // Sign-in success
                                    Toast.makeText(LoginScreen.this, "Authentication Successful.", Toast.LENGTH_SHORT).show();
                                    startActivity(new Intent(LoginScreen.this, MainActivity.class));
                                    finish();
                                } else {
                                    // If sign-in fails
                                    Toast.makeText(LoginScreen.this, "Authentication failed: " + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
}
