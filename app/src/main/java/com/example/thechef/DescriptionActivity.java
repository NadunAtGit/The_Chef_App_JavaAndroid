package com.example.thechef;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DescriptionActivity extends AppCompatActivity {

    private TextView foodNameTxt, foodDescriptionTxt, stepsTxt;
    private ConstraintLayout ingredientsContainer;
    private ImageView foodImage;
    private Button save;
    private String recipeId;
    private DatabaseReference savedRecipesRef;
    private FirebaseAuth mAuth; // For user authentication

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        foodNameTxt = findViewById(R.id.foodName);
        save = findViewById(R.id.save);
        foodDescriptionTxt = findViewById(R.id.foodDescription);
        stepsTxt = findViewById(R.id.Steps);
        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        foodImage = findViewById(R.id.foodImage);

        recipeId = getIntent().getStringExtra("recipeId");

        // Firebase database reference for SavedRecipes
        savedRecipesRef = FirebaseDatabase.getInstance().getReference("SavedRecipes");

        if (recipeId != null) {
            fetchRecipeDataFromFirebase(recipeId);
        }

        // Set click listener for the save button
        save.setOnClickListener(v -> saveRecipe(recipeId));
    }
    private void fetchRecipeDataFromFirebase(String recipeId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference recipeRef = database.getReference("recipes").child(recipeId);

        recipeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the food name, description, image URL, ingredients, steps, and other fields
                    String foodName = dataSnapshot.child("foodName").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                    String steps = dataSnapshot.child("steps").getValue(String.class);
                    String time = dataSnapshot.child("time").getValue(String.class);
                    long ratingCount = dataSnapshot.child("ratingCount").getValue(Long.class);
                    long score = dataSnapshot.child("score").getValue(Long.class);

                    // Set the text in the TextViews
                    foodNameTxt.setText(foodName);
                    foodDescriptionTxt.setText(description);
                    stepsTxt.setText(steps);

                    // Load the image using Glide
                    Glide.with(DescriptionActivity.this)
                            .load(imageUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_delete)
                            .into(foodImage);

                    // Clear existing views in the ingredients container
                    ingredientsContainer.removeAllViews();

                    // Dynamically populate the ingredients
                    for (DataSnapshot ingredientSnapshot : dataSnapshot.child("ingredients").getChildren()) {
                        String ingredientName = ingredientSnapshot.getKey();
                        String ingredientQuantity = ingredientSnapshot.getValue(String.class);

                        // Create a new TextView for each ingredient
                        TextView ingredientTextView = new TextView(DescriptionActivity.this);
                        ingredientTextView.setText(ingredientName + ": " + ingredientQuantity);
                        ingredientTextView.setTextSize(16);

                        // Add the TextView to the ConstraintLayout
                        ingredientsContainer.addView(ingredientTextView);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseData", "Error retrieving data: " + databaseError.getMessage());
            }
        });
    }


    // Save recipe method
    // Save recipe method
    // Save recipe method
    private void saveRecipe(String recipeId) {
        String userId = mAuth.getCurrentUser().getUid(); // Get current user's ID

        // Print userId to the console for debugging
        Log.d("UserID", "Current User ID: " + userId); // 'UserID' is the tag for easy filtering in logcat

        // Save the recipe under the user's node in Firebase (SavedRecipes > U-0001 > R-0001: true)
        savedRecipesRef.child(userId).child(recipeId).setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(DescriptionActivity.this, "Recipe saved successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DescriptionActivity.this, "Failed to save recipe.", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
