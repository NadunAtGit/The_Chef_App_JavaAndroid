package com.example.thechef;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.thechef.Domain.RecipeDomain;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddRecipe extends AppCompatActivity {

    private EditText foodNameField, descriptionField, Time, Steps, ingredientsField;
    private TextView uploadStatus;
    private Button submitButton, addImageButton, addVideoButton;
    private Spinner categorySpinner;
    private ProgressBar progressBar2; // Add progress bar
    private ImageView uploadImg;
    private FirebaseDatabase database;
    private DatabaseReference recipeRef;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri imageUri = null;
    private Uri videoUri = null;

    private FirebaseAuth mAuth;



    // Inside the AddRecipe class

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        // Initialize Firebase database and storage
        database = FirebaseDatabase.getInstance();
        recipeRef = database.getReference("recipes");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        foodNameField = findViewById(R.id.editName);
        uploadStatus = findViewById(R.id.uploadStatus);
        uploadImg = findViewById(R.id.uploadImg);
        Steps = findViewById(R.id.editSteps);
        descriptionField = findViewById(R.id.editDescription);
        ingredientsField = findViewById(R.id.editIngredients);
        submitButton = findViewById(R.id.updateButton);
        addImageButton = findViewById(R.id.uploadImageButton);
        Time = findViewById(R.id.editTime);
        categorySpinner = findViewById(R.id.categorySpinner);
        progressBar2 = findViewById(R.id.progressBar2); // Find the progress bar by ID

        // Hide progress bar initially
        progressBar2.setVisibility(View.GONE);

        // Image picker launcher
        ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        Toast.makeText(AddRecipe.this, "Image selected!", Toast.LENGTH_SHORT).show();

                        // Load the selected image into the ImageView
                        Glide.with(this)
                                .load(imageUri)
                                .into(uploadImg);
                    }
                }
        );

        // Video picker launcher
        ActivityResultLauncher<Intent> videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        videoUri = result.getData().getData();
                        Toast.makeText(AddRecipe.this, "Video selected!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Handle image selection
        addImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        addVideoButton = findViewById(R.id.uploadVideoButton);
        addVideoButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            videoPickerLauncher.launch(intent); // Use the defined video launcher
        });

        // Handle the submit button
        submitButton.setOnClickListener(v -> {
            // Validate the fields before uploading
            if (isInputValid()) {
                if (imageUri != null && videoUri != null) {
                    // Show progress bar when upload starts
                    progressBar2.setVisibility(View.VISIBLE);
                    uploadImageToFirebase(imageUri);  // This will trigger video upload after the image upload succeeds
                } else {
                    Toast.makeText(this, "Please select both image and video.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }





    // Method to validate input fields
    private boolean isInputValid() {
        String foodName = foodNameField.getText().toString().trim();
        String description = descriptionField.getText().toString().trim();
        String ingredients = ingredientsField.getText().toString().trim();
        String time = Time.getText().toString().trim();
        String steps = Steps.getText().toString().trim();

        if (foodName.isEmpty()) {
            foodNameField.setError("Food name is required");
            return false;
        }
        if (description.isEmpty()) {
            descriptionField.setError("Description is required");
            return false;
        }
        if (ingredients.isEmpty()) {
            ingredientsField.setError("Ingredients are required");
            return false;
        }
        if (time.isEmpty()) {
            Time.setError("Cooking time is required");
            return false;
        }
        if (steps.isEmpty()) {
            Steps.setError("Steps are required");
            return false;
        }
        return true; // All validations passed
    }
    private void clearFields() {
        foodNameField.setText("");
        descriptionField.setText("");
        ingredientsField.setText("");
        Time.setText("");
        Steps.setText("");
        uploadImg.setImageResource(0); // or set to a placeholder image
        uploadStatus.setText(""); // Optional: Reset upload status
        categorySpinner.setSelection(0); // Reset spinner to default selection
    }

    private void uploadImageToFirebase(Uri uri) {
        String fileName = foodNameField.getText().toString().trim().replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference fileRef = storageReference.child("recipes/" + fileName);

        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    String imageUrl = downloadUri.toString();

                    // Now upload the video after image upload succeeds
                    uploadVideoToFirebase(videoUri, imageUrl);

                }))
                .addOnFailureListener(e -> {
                    progressBar2.setVisibility(View.GONE);
                    Toast.makeText(AddRecipe.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadVideoToFirebase(Uri uri, String imageUrl) {
        String videoName = foodNameField.getText().toString().trim().replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + ".mp4";
        StorageReference videoRef = storageReference.child("recipes_videos/" + videoName);

        videoRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> videoRef.getDownloadUrl().addOnSuccessListener(videoDownloadUri -> {
                    String videoUrl = videoDownloadUri.toString();

                    // Call the method to upload the recipe with both imageUrl and videoUrl
                    uploadRecipeWithCustomID(imageUrl, videoUrl);
                }))
                .addOnFailureListener(e -> {
                    progressBar2.setVisibility(View.GONE);
                    Toast.makeText(AddRecipe.this, "Failed to upload video: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private void uploadRecipeWithCustomID(String imageUrl, String videoUrl) {
        String foodName = foodNameField.getText().toString().trim();
        String description = descriptionField.getText().toString().trim();
        String time1 = Time.getText().toString().trim();
        String steps = Steps.getText().toString().trim();
        String selectedCategory = categorySpinner.getSelectedItem().toString();
        String ingredients = ingredientsField.getText().toString().trim();
        String userId = mAuth.getCurrentUser().getUid();

        Double score = 0.0;
        int ratingCount = 0;

        if (foodName.isEmpty() || description.isEmpty() || ingredients.isEmpty()) {
            Toast.makeText(this, "Please enter food name, description, and ingredients.", Toast.LENGTH_SHORT).show();
            return;
        }

        recipeRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String lastId = "";
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        lastId = snapshot.getKey();
                    }
                }
                String newId = generateNextId(lastId);

                RecipeDomain recipe = new RecipeDomain(newId, foodName, description, imageUrl, videoUrl, time1, score, ratingCount, ingredients, steps, selectedCategory, userId);

                Map<String, Object> recipeMap = new HashMap<>();
                recipeMap.put("foodName", recipe.getFoodName());
                recipeMap.put("description", recipe.getDescription());
                recipeMap.put("time", recipe.getTime());
                recipeMap.put("score", recipe.getScore());
                recipeMap.put("imageUrl", imageUrl);
                recipeMap.put("videoUrl", videoUrl);  // Add video URL to the map
                recipeMap.put("steps", steps);
                recipeMap.put("RatingCount", ratingCount);
                recipeMap.put("ingredients", ingredients);
                recipeMap.put("category", selectedCategory);
                recipeMap.put("userId", userId);

                recipeRef.child(newId).setValue(recipeMap)
                        .addOnSuccessListener(aVoid -> {
                            progressBar2.setVisibility(View.GONE);
                            Toast.makeText(AddRecipe.this, "Recipe uploaded successfully!", Toast.LENGTH_SHORT).show();
                            clearFields();
                        })
                        .addOnFailureListener(e -> {
                            progressBar2.setVisibility(View.GONE);
                            Toast.makeText(AddRecipe.this, "Failed to upload recipe.", Toast.LENGTH_SHORT).show();
                        });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AddRecipe.this, "Error fetching last ID: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private String generateNextId(String lastId) {
        if (lastId.isEmpty()) {
            return "R-0001";
        }
        String[] parts = lastId.split("-");
        int nextId = Integer.parseInt(parts[1]) + 1;
        return String.format(Locale.getDefault(), "R-%04d", nextId);
    }
}
