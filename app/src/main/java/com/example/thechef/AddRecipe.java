package com.example.thechef;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.thechef.Domain.RecipeDomain;
import com.google.firebase.auth.FirebaseAuth; // Import FirebaseAuth to get current user ID
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class AddRecipe extends AppCompatActivity {

    private EditText foodNameField, descriptionField, Time, Steps, ingredientsField; // Changed ingredientsContainer to a single EditText
    private Button submitButton, addImageButton;
    private Spinner categorySpinner; // Add spinner for category selection

    private FirebaseDatabase database;
    private DatabaseReference recipeRef;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri imageUri = null;
    private FirebaseAuth mAuth; // FirebaseAuth instance to get current user ID

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        // Initialize Firebase database and storage
        database = FirebaseDatabase.getInstance();
        recipeRef = database.getReference("recipes");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        mAuth = FirebaseAuth.getInstance(); // Initialize FirebaseAuth

        // Initialize views
        foodNameField = findViewById(R.id.editName);
        Steps = findViewById(R.id.editSteps);
        descriptionField = findViewById(R.id.editDescription);
        ingredientsField = findViewById(R.id.editIngredients); // Single EditText for ingredients
        submitButton = findViewById(R.id.updateButton);
        addImageButton = findViewById(R.id.uploadImageButton);
        Time = findViewById(R.id.editTime);
        categorySpinner = findViewById(R.id.categorySpinner); // Initialize category spinner

        // Image picker launcher
        ActivityResultLauncher<Intent> imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        Toast.makeText(AddRecipe.this, "Image selected!", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        // Handle image selection
        addImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        // Handle the submit button
        submitButton.setOnClickListener(v -> {
            if (imageUri != null) {
                uploadImageToFirebase(imageUri);
            } else {
                Toast.makeText(this, "Please select an image first.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void uploadImageToFirebase(Uri uri) {
        // Create a reference for the image file in Firebase Storage
        String fileName = foodNameField.getText().toString().trim().replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference fileRef = storageReference.child("recipes/" + fileName);

        // Start the upload
        fileRef.putFile(uri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                    // The image is successfully uploaded, and we get the download URL
                    String imageUrl = downloadUri.toString();
                    uploadRecipeWithCustomID(imageUrl); // Upload recipe data with the image URL
                }))
                .addOnFailureListener(e -> {
                    // Handle the failure
                    Toast.makeText(AddRecipe.this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void uploadRecipeWithCustomID(String imageUrl) {
        String foodName = foodNameField.getText().toString().trim();
        String description = descriptionField.getText().toString().trim();
        String time1 = Time.getText().toString().trim();
        String steps = Steps.getText().toString().trim(); // Capture steps here
        String selectedCategory = categorySpinner.getSelectedItem().toString(); // Get selected category
        String ingredients = ingredientsField.getText().toString().trim(); // Get ingredients from single input
        String userId = mAuth.getCurrentUser().getUid(); // Get current user ID

        Double score = 0.0;
        int ratingCount = 0;

        if (foodName.isEmpty() || description.isEmpty() || ingredients.isEmpty()) {
            Toast.makeText(this, "Please enter food name, description, and ingredients.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve the last recipe ID and generate the next custom ID
        recipeRef.orderByKey().limitToLast(1).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String lastId = "";
                if (dataSnapshot.exists()) {
                    // Get the last ID
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        lastId = snapshot.getKey();
                    }
                }
                String newId = generateNextId(lastId);

                // Create a RecipeDomain object including the category and userId
                RecipeDomain recipe = new RecipeDomain(newId, foodName, description, imageUrl, time1, score, ratingCount, ingredients, steps, selectedCategory, userId); // Include selectedCategory and userId

                // Prepare the recipe map
                Map<String, Object> recipeMap = new HashMap<>();
                recipeMap.put("foodName", recipe.getFoodName());
                recipeMap.put("description", recipe.getDescription());
                recipeMap.put("time", recipe.getTime());
                recipeMap.put("score", recipe.getScore());
                recipeMap.put("imageUrl", imageUrl); // Include the image URL here
                recipeMap.put("steps", steps); // Add steps to the map
                recipeMap.put("RatingCount", ratingCount);
                recipeMap.put("ingredients", ingredients); // Add ingredients to the map
                recipeMap.put("category", selectedCategory); // Add selected category to the map
                recipeMap.put("userId", userId); // Add userId to the map

                // Push to Firebase using custom ID
                recipeRef.child(newId).setValue(recipeMap)
                        .addOnSuccessListener(aVoid -> Toast.makeText(AddRecipe.this, "Recipe uploaded successfully!", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(AddRecipe.this, "Failed to upload recipe.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(AddRecipe.this, "Error fetching last ID: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Helper method to generate the next ID in the format R-0001, R-0002, etc.
    private String generateNextId(String lastId) {
        if (lastId.isEmpty()) {
            return "R-0001"; // Starting ID
        }
        String[] parts = lastId.split("-");
        int nextId = Integer.parseInt(parts[1]) + 1;
        return String.format(Locale.getDefault(), "R-%04d", nextId);
    }
}
