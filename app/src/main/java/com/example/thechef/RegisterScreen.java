package com.example.thechef;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class RegisterScreen extends AppCompatActivity {
    EditText name, password, email, confirm_password;
    private static final String TAG = "RegisterScreen";
    FirebaseAuth mAuth;
    Button google;

    ProgressBar progressBar;
    DatabaseReference databaseReference;  // Firebase Database Reference

    @Override
    public void onStart() {
        super.onStart();
        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Check if a user is currently signed in
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            // If there is a logged-in user, redirect to MainActivity
            startActivity(new Intent(RegisterScreen.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_screen);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");  // Firebase Database reference

        // Set padding for system bars
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize UI elements
        TextView su = findViewById(R.id.signup);
        Button th = findViewById(R.id.toHome);
        name = findViewById(R.id.name);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirm_password = findViewById(R.id.con_password);
        progressBar = findViewById(R.id.progress);

        // Initially hide the progress bar
        progressBar.setVisibility(View.GONE);

        // Redirect to LoginScreen on click of signup text
        su.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(RegisterScreen.this, LoginScreen.class));
                finish();
            }
        });

        // Handle register button click
        th.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show progress bar when registration starts
                progressBar.setVisibility(View.VISIBLE);

                String emailV = email.getText().toString().trim();
                String passwordV = password.getText().toString().trim();
                String confirmPasswordV = confirm_password.getText().toString().trim();

                // Validate input fields
                if (TextUtils.isEmpty(emailV)) {
                    Toast.makeText(RegisterScreen.this, "Please Enter Email", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (TextUtils.isEmpty(passwordV)) {
                    Toast.makeText(RegisterScreen.this, "Please Enter Password", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }
                if (!passwordV.equals(confirmPasswordV)) {
                    Toast.makeText(RegisterScreen.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                    return;
                }

                // Create a new user with Firebase Authentication
                mAuth.createUserWithEmailAndPassword(emailV, passwordV)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                // Hide progress bar after registration is completed
                                progressBar.setVisibility(View.GONE);

                                if (task.isSuccessful()) {
                                    // Firebase Authentication success
                                    FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                    if (firebaseUser != null) {
                                        String userId = firebaseUser.getUid();
                                        saveUserDetails(userId, name.getText().toString(), emailV, passwordV);
                                    }
                                } else {
                                    // If sign in fails
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                    Toast.makeText(RegisterScreen.this, "Authentication failed: " + task.getException().getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    // Save user details to Firebase Database
    private void saveUserDetails(String firebaseUserId, String name, String email, String password) {
        // Get the last user ID from Firebase Database and generate the next one
        databaseReference.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String lastUserId = "U-000";  // Default value if no users exist

                // Get the last user ID
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    lastUserId = snapshot.getKey();
                }

                // Generate the next user ID
                String newUserId = getNextUserId(lastUserId);

                // Create a new User object
                User user = new User(newUserId, name, email, password);

                // Save user data to Firebase
                databaseReference.child(newUserId).setValue(user)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                Toast.makeText(RegisterScreen.this, "User details saved successfully.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterScreen.this, MainActivity.class));
                                finish();
                            } else {
                                Toast.makeText(RegisterScreen.this, "Failed to save user details.", Toast.LENGTH_SHORT).show();
                            }
                        });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e(TAG, "Failed to retrieve last user ID.", databaseError.toException());
            }
        });
    }

    // Generate the next user ID
    private String getNextUserId(String lastUserId) {
        String idNumber = lastUserId.substring(2);  // Remove "U-"
        int nextIdNumber = Integer.parseInt(idNumber) + 1;
        return String.format(Locale.getDefault(), "U-%03d", nextIdNumber);
    }
}







