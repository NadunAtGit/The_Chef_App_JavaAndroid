package com.example.thechef;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;

public class ForgotPassword extends AppCompatActivity {
    private ImageView backButton;
    private EditText recoveryMail;
    private Button sendLink;
    private FirebaseAuth auth; // Declare FirebaseAuth

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);

        // Initialize FirebaseAuth
        auth = FirebaseAuth.getInstance();

        // Initialize UI elements
        backButton = findViewById(R.id.backButton);
        recoveryMail = findViewById(R.id.recoveryMail);
        sendLink = findViewById(R.id.sendLink);

        // Back button click listener
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(ForgotPassword.this, LoginScreen.class));
            }
        });

        // Send link button click listener
        sendLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = recoveryMail.getText().toString().trim();

                if (email.isEmpty()) {
                    Toast.makeText(ForgotPassword.this, "Please enter your email", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Send password reset email
                auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(ForgotPassword.this, "Recovery link sent to your email", Toast.LENGTH_SHORT).show();
                                // Optionally navigate back to login screen
                                startActivity(new Intent(ForgotPassword.this, LoginScreen.class));
                                finish(); // Close the current activity
                            } else {
                                Toast.makeText(ForgotPassword.this, "Failed to send recovery link", Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });

        // Apply window insets for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}
