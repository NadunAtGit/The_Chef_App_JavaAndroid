package com.example.thechef;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class profileActivity extends AppCompatActivity {

    private TextView username, email;
    private ImageView profilepic;
    private Button logout;
    private Button updateProfile;
    private FirebaseAuth auth;
    private FirebaseDatabase database;
    private DatabaseReference userRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth and Database
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        // Initialize UI components
        username = findViewById(R.id.username);
        email = findViewById(R.id.email);
        profilepic = findViewById(R.id.profilepic);
        logout = findViewById(R.id.logout);
        updateProfile = findViewById(R.id.updateProfile);

        // Check if user is logged in
        if (user != null) {
            String userId = user.getUid();
            String emailAddress = user.getEmail();

            email.setText(emailAddress); // Set email

            // Check if user signed in via Google or email/password
            if (user.getProviderData().get(1).getProviderId().equals("google.com")) {
                // Handle Google signed-in user
                loadGoogleUserProfile(user);
            } else {
                // Handle Email/Password signed-in user
                loadEmailUserProfile(userId);
            }
        }

        // Logout functionality
        logout.setOnClickListener(v -> {
            auth.signOut();
            Toast.makeText(profileActivity.this, "Logged out successfully.", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(profileActivity.this, LoginScreen.class);
            startActivity(intent);
            finish();
        });

        // Hide Update Profile button for Google signed users
        if (user != null && user.getProviderData().get(1).getProviderId().equals("google.com")) {
            updateProfile.setVisibility(View.GONE); // Hide update profile button for Google users
        } else {
            // Show update profile button and handle click for email/password users
            updateProfile.setOnClickListener(v -> {
                Intent intent = new Intent(profileActivity.this, updateProfile.class);
                startActivity(intent);
            });
        }
    }

    private void loadGoogleUserProfile(FirebaseUser user) {
        // Get Google profile photo
        Uri googleProfilePhoto = user.getPhotoUrl();
        String displayName = user.getDisplayName();

        username.setText(displayName);  // Set Google username

        if (googleProfilePhoto != null) {
            Glide.with(this)
                    .load(googleProfilePhoto)
                    .circleCrop()
                    .into(profilepic);  // Load profile photo using Glide
        }
    }

    private void loadEmailUserProfile(String userId) {
        userRef = database.getReference("Users").child(userId);

        // Retrieve the user data from Firebase Database for email/password users
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String name = snapshot.child("name").getValue(String.class);
                    String profileUrl = snapshot.child("imageUrl").getValue(String.class); // Retrieve imageUrl from Firebase

                    username.setText(name);  // Set email/password user name

                    if (profileUrl != null && !profileUrl.isEmpty()) {
                        // Load profile photo using Glide
                        Glide.with(profileActivity.this)
                                .load(profileUrl)
                                .circleCrop()
                                .into(profilepic);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(profileActivity.this, "Failed to load user data.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
