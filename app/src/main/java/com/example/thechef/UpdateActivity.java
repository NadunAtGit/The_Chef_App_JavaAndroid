package com.example.thechef;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

public class UpdateActivity extends AppCompatActivity {

    private EditText editName, editTime, editDescription, editIngredients, editSteps;
    private ImageView imageRecipe;
    private Button updateButton, uploadImageButton;
    private Uri imageUri;
    private String imageUrl;
    private String recipeId;
    private DatabaseReference recipeRef;
    private StorageReference storageRef;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        // Initialize UI elements
        editName = findViewById(R.id.editName);
        editTime = findViewById(R.id.editTime);
        editDescription = findViewById(R.id.editDescription);
        editIngredients = findViewById(R.id.editIngredients);
        editSteps = findViewById(R.id.editSteps);

        updateButton = findViewById(R.id.updateButton);
        uploadImageButton = findViewById(R.id.uploadImageButton);

        // Get recipe ID from the intent
        recipeId = getIntent().getStringExtra("recipeId");

        // Firebase references
        recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);
        storageRef = FirebaseStorage.getInstance().getReference("recipes");



        // Fetch existing data and populate EditTexts
        fetchRecipeData();

        // Handle image upload
        uploadImageButton.setOnClickListener(v -> openGallery());

        // Handle update button click
        updateButton.setOnClickListener(v -> updateRecipe());
    }

    private void fetchRecipeData() {
        recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Fetch data from Firebase
                    String foodName = dataSnapshot.child("foodName").getValue(String.class);
                    String time = dataSnapshot.child("time").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    String ingredients = dataSnapshot.child("ingredients").getValue(String.class);
                    String steps = dataSnapshot.child("steps").getValue(String.class);
                    imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);

                    // Populate EditTexts with the fetched data
                    editName.setText(foodName);
                    editTime.setText(time);
                    editDescription.setText(description);
                    editIngredients.setText(ingredients);
                    editSteps.setText(steps);

                    // Load the image using Glide
//                    Glide.with(UpdateActivity.this).load(imageUrl).into(imageRecipe);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(UpdateActivity.this, "Error fetching data.", Toast.LENGTH_SHORT).show();
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
            imageRecipe.setImageURI(imageUri); // Show the selected image
        }
    }

    private void updateRecipe() {


        if (imageUri != null) {
            // Upload new image if the user selects one
            StorageReference filePath = storageRef.child(recipeId + ".jpg");
            filePath.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                filePath.getDownloadUrl().addOnSuccessListener(uri -> {
                    imageUrl = uri.toString();
                    updateRecipeData();
                });
            });
        } else {
            // If no new image is selected, just update the other data
            updateRecipeData();
        }
    }

    private void updateRecipeData() {
        HashMap<String, Object> recipeMap = new HashMap<>();
        recipeMap.put("foodName", editName.getText().toString());
        recipeMap.put("time", editTime.getText().toString());
        recipeMap.put("description", editDescription.getText().toString());
        recipeMap.put("ingredients", editIngredients.getText().toString());
        recipeMap.put("steps", editSteps.getText().toString());
        recipeMap.put("imageUrl", imageUrl); // Use the updated or old imageUrl

        recipeRef.updateChildren(recipeMap).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {

                Toast.makeText(UpdateActivity.this, "Recipe updated successfully.", Toast.LENGTH_SHORT).show();

                finish(); // Close the activity after updating
            } else {

                Toast.makeText(UpdateActivity.this, "Failed to update recipe.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
