package com.example.thechef;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

public class updateProfile extends AppCompatActivity {

    private ImageView updateImage, profilePic;
    private EditText updateName, updatePW, updatePWcon;
    private Button updateProfile;
    private Uri imageUri;
    private String imageUrl;
    private String userId;
    private DatabaseReference userRef;
    private StorageReference storageRef;
    private FirebaseAuth mAuth;

    private static final String TAG = "updateProfile";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        updateImage = findViewById(R.id.updateImage);
        updateName = findViewById(R.id.updateName);
        profilePic = findViewById(R.id.profilepic);
        updatePW = findViewById(R.id.updatePW);
        updatePWcon = findViewById(R.id.updatePWcon);
        updateProfile = findViewById(R.id.updateProfile);

        // Initialize Firebase Auth and Database references
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            Toast.makeText(this, "No authenticated user", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        userId = currentUser.getUid();
        String email = currentUser.getEmail(); // Retain the email if needed

        userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        storageRef = FirebaseStorage.getInstance().getReference("ProfilePics");

        // Handle image upload
        updateImage.setOnClickListener(v -> openGallery());

        // Handle profile update button click
        updateProfile.setOnClickListener(v -> updateUserProfile(email));

        // Optionally, load the current profile image
        loadCurrentProfileImage();
    }

    private void loadCurrentProfileImage() {
        userRef.child("imageUrl").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                imageUrl = snapshot.getValue(String.class);
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(updateProfile.this)
                            .load(imageUrl)
                            .circleCrop()
                            .into(profilePic);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "Failed to load profile image.", error.toException());
                Toast.makeText(updateProfile.this, "Failed to load profile image.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, 1);
    }

    @Override

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();

            // Show the selected image in both ImageViews
            profilePic.setImageURI(imageUri); // Show the selected image in updateImage
            Glide.with(this)
                    .load(imageUri) // Use Glide to load image into profilePic
                    .circleCrop()
                    .into(profilePic); // Show the selected image in profilePic
        } else {
            Toast.makeText(this, "Image selection failed.", Toast.LENGTH_SHORT).show();
        }
    }


    private void updateUserProfile(String email) {
        // Validate password fields
        String password = updatePW.getText().toString().trim();
        String confirmPassword = updatePWcon.getText().toString().trim();

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validate name and password are not empty
        String name = updateName.getText().toString().trim();
        if (name.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Username and password must not be empty", Toast.LENGTH_SHORT).show();
            return;
        }

        if (imageUri != null) {
            // Upload new image
            StorageReference filePath = storageRef.child(userId + ".jpg");
            filePath.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrl = uri.toString(); // Update image URL
                    Log.d(TAG, "Image uploaded: " + imageUrl);
                    updateUserData(email, password); // Pass password for updating
                }).addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get download URL", e);
                    Toast.makeText(updateProfile.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Image upload failed", e);
                Toast.makeText(updateProfile.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        } else {
            // If no new image is selected, just update other fields
            updateUserData(email, password); // Pass password for updating
        }
    }

    private void updateUserData(String email, String password) {
        String name = updateName.getText().toString().trim();

        // Prepare user data to update
        HashMap<String, Object> userMap = new HashMap<>();
        userMap.put("name", name);
        userMap.put("email", email); // Update email if needed

        if (imageUrl != null) {
            userMap.put("imageUrl", imageUrl); // Update image URL
        }

        Log.d(TAG, "Updating user data: " + userMap.toString());

        // Update the user info in the database
        userRef.updateChildren(userMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "User data updated successfully.");
                updatePassword(password); // Call to update password after user data
            } else {
                Log.e(TAG, "Failed to update user data.", task.getException());
                Toast.makeText(updateProfile.this, "Failed to update profile.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePassword(String newPassword) {
        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            user.updatePassword(newPassword).addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(updateProfile.this, "Password updated successfully.", Toast.LENGTH_SHORT).show();
                    finish(); // Close activity after updating
                } else {
                    Log.e(TAG, "Password update failed.", task.getException());
                    Toast.makeText(updateProfile.this, "Password update failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(this, "User not authenticated.", Toast.LENGTH_SHORT).show();
        }
    }
}
