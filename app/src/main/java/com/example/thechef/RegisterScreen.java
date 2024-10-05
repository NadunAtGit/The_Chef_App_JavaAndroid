package com.example.thechef;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;

public class RegisterScreen extends AppCompatActivity {
    private static final String TAG = "RegisterScreen";
    private static final int PICK_IMAGE_REQUEST = 1;  // Request code for image picker
    private FirebaseAuth mAuth;
    private DatabaseReference databaseReference;  // Firebase Database Reference
    private StorageReference storageReference;  // Firebase Storage Reference

    private EditText name, password, email, confirm_password;
    private ImageView profilePic;
    private Uri imageUri;  // Uri to hold the selected image

    private ProgressBar progressBar;

    @Override
    public void onStart() {
        super.onStart();
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            startActivity(new Intent(RegisterScreen.this, MainActivity.class));
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_screen);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference("ProfilePics");  // Initialize Firebase Storage Reference

        profilePic = findViewById(R.id.profilepic);
        Button registerBtn = findViewById(R.id.toHome);
        progressBar = findViewById(R.id.progress);
        name = findViewById(R.id.editName);
        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        confirm_password = findViewById(R.id.con_password);

        // Initially hide the progress bar
        progressBar.setVisibility(View.GONE);

        // Load default circular image
        loadCircularImage(R.drawable.user, profilePic);

        // Set OnClickListener to open the image picker when the profile picture is clicked
        profilePic.setOnClickListener(v -> openImagePicker());

        // Register button click listener
        registerBtn.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);

            String emailV = email.getText().toString().trim();
            String passwordV = password.getText().toString().trim();
            String confirmPasswordV = confirm_password.getText().toString().trim();

            if (validateInputs(emailV, passwordV, confirmPasswordV)) {
                mAuth.createUserWithEmailAndPassword(emailV, passwordV)
                        .addOnCompleteListener(task -> {
                            progressBar.setVisibility(View.GONE);
                            if (task.isSuccessful()) {
                                FirebaseUser firebaseUser = mAuth.getCurrentUser();
                                if (firebaseUser != null) {
                                    // Save the image to Firebase Storage and user details to Firebase Database
                                    uploadImageAndSaveDetails(firebaseUser.getUid(), emailV);
                                }
                            } else {
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterScreen.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    // Method to open image picker
    private void openImagePicker() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    // Handle result from image picker
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            imageUri = data.getData();
            Glide.with(this)
                    .load(imageUri)
                    .circleCrop() // Apply circular cropping
                    .into(profilePic);
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                profilePic.setImageBitmap(bitmap);  // Set the selected image to ImageView
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Validate input fields
    private boolean validateInputs(String emailV, String passwordV, String confirmPasswordV) {
        if (TextUtils.isEmpty(emailV)) {
            Toast.makeText(RegisterScreen.this, "Please Enter Email", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return false;
        }
        if (TextUtils.isEmpty(passwordV)) {
            Toast.makeText(RegisterScreen.this, "Please Enter Password", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return false;
        }
        if (!passwordV.equals(confirmPasswordV)) {
            Toast.makeText(RegisterScreen.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return false;
        }
        return true;
    }

    // Upload selected image to Firebase Storage and save user details to Firebase Database
    private void uploadImageAndSaveDetails(String userId, String email) {
        if (imageUri != null) {
            StorageReference fileReference = storageReference.child(userId + "_" + UUID.randomUUID().toString());
            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        // Get the download URL and save user details
                        saveUserDetails(userId, name.getText().toString(), email, uri.toString());
                    }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(RegisterScreen.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            // No image selected, save user details without profile pic URL
            saveUserDetails(userId, name.getText().toString(), email, null);
        }
    }

    // Save user details to Firebase Database
    private void saveUserDetails(String firebaseUserId, String name, String email, String profilePicUrl) {
        User user = new User(firebaseUserId, name, email, profilePicUrl);
        databaseReference.child(firebaseUserId).setValue(user)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(RegisterScreen.this, "User registered successfully.", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterScreen.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(RegisterScreen.this, "Failed to register user.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Load circular image using Glide
    private void loadCircularImage(int imageResId, ImageView imageView) {
        Glide.with(this)
                .load(imageResId)
                .circleCrop()
                .into(imageView);
    }
}
