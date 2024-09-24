package com.example.thechef;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import java.util.Locale;

import java.util.HashMap;
import java.util.Map;

public class AddRecipe extends AppCompatActivity {

    private EditText foodNameField, descriptionField,Time;
    private LinearLayout ingredientsContainer;
    private Button addFieldButton, submitButton, addImageButton;

    private int fieldCounter = 1;  // To track the number of ingredient fields
    private FirebaseDatabase database;
    private DatabaseReference recipeRef;
    private FirebaseStorage storage;
    private StorageReference storageReference;
    private Uri imageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_recipe);

        // Initialize Firebase database and storage
        database = FirebaseDatabase.getInstance();
        recipeRef = database.getReference("recipes");
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();

        // Initialize views
        foodNameField = findViewById(R.id.name);
        descriptionField = findViewById(R.id.description);
        ingredientsContainer = findViewById(R.id.ingredients_container);
        addFieldButton = findViewById(R.id.addField);
        submitButton = findViewById(R.id.submit);
        addImageButton = findViewById(R.id.addImage);
        Time=findViewById(R.id.time);

        // Add dynamic fields for ingredients
        addFieldButton.setOnClickListener(v -> addNewField());

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

    // Method to dynamically add a new ingredient field
    private void addNewField() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View newField = inflater.inflate(R.layout.ingredient_field, ingredientsContainer, false);

        EditText ingredientField = newField.findViewById(R.id.ingredient_name);
        EditText quantityField = newField.findViewById(R.id.ingredient_quantity);

        ingredientField.setHint("Ingredient " + fieldCounter);
        quantityField.setHint("Quantity " + fieldCounter);

        ingredientsContainer.addView(newField);
        fieldCounter++;
    }

    private void uploadImageToFirebase(Uri uri) {
        // Create a reference for the image file in Firebase Storage
        String fileName = foodNameField.getText().toString().trim().replaceAll("\\s+", "_") + "_" + System.currentTimeMillis() + ".jpg";
        StorageReference fileRef = storageReference.child("recipes/" + fileName);
        double scr=0;


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
        String time1=Time.getText().toString().trim();
        Double score=0.0;

        if (foodName.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Please enter food name and description.", Toast.LENGTH_SHORT).show();
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

                // Create a RecipeClass object
                RecipeClass recipe = new RecipeClass(foodName, description,time1,score);

                // Capture each ingredient and quantity from the dynamically added fields
                for (int i = 0; i < ingredientsContainer.getChildCount(); i++) {
                    View ingredientView = ingredientsContainer.getChildAt(i);

                    EditText ingredientField = ingredientView.findViewById(R.id.ingredient_name);
                    EditText quantityField = ingredientView.findViewById(R.id.ingredient_quantity);

                    String ingredientName = ingredientField.getText().toString().trim();
                    String quantity = quantityField.getText().toString().trim();

                    if (!ingredientName.isEmpty() && !quantity.isEmpty()) {
                        recipe.addIngredient(ingredientName, quantity);
                    }
                }

                // Prepare the recipe map
                Map<String, Object> recipeMap = new HashMap<>();
                recipeMap.put("foodName", recipe.getFoodName());
                recipeMap.put("description", recipe.getDescription());
                recipeMap.put("time", recipe.getTime());
                recipeMap.put("score", recipe.getScore());
                recipeMap.put("imageUrl", imageUrl); // Include the image URL here

                // Convert list of ingredients to a map
                Map<String, String> ingredientsMap = new HashMap<>();
                for (Ingredient ingredient : recipe.getIngredients()) {
                    ingredientsMap.put(ingredient.getIngredientName(), ingredient.getQuantity());
                }
                recipeMap.put("ingredients", ingredientsMap);

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
            return "R-0001";  // Start with R-0001 if no ID exists
        }

        // Extract the numeric part of the last ID and increment it
        int lastNumericId = Integer.parseInt(lastId.substring(2));  // Skip the "R-"
        int newNumericId = lastNumericId + 1;

        // Return the new ID in the format R-xxxx (e.g., R-0002)
        return String.format(Locale.getDefault(), "R-%04d", newNumericId);
    }
}
