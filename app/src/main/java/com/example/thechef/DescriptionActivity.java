package com.example.thechef;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DescriptionActivity extends AppCompatActivity {

    private TextView foodNameTxt, foodDescriptionTxt, stepsTxt; // Added steps TextView
    private ConstraintLayout ingredientsContainer;
    private ImageView foodImage;
    private String recipeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        // Initialize TextViews
        foodNameTxt = findViewById(R.id.foodName);
        foodDescriptionTxt = findViewById(R.id.foodDescription);
        stepsTxt = findViewById(R.id.Steps); // Initialize the steps TextView
        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        foodImage = findViewById(R.id.foodImage);

        recipeId = getIntent().getStringExtra("recipeId");

        if (recipeId != null) {
            fetchRecipeDataFromFirebase(recipeId);
        }
    }

    private void fetchRecipeDataFromFirebase(String recipeId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference recipeRef = database.getReference("recipes").child(recipeId);

        recipeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Get the food name, description, image URL, ingredients, and steps
                    String foodName = dataSnapshot.child("foodName").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                    String steps = dataSnapshot.child("steps").getValue(String.class); // Fetching steps

                    // Set the text in the TextViews
                    foodNameTxt.setText(foodName);
                    foodDescriptionTxt.setText(description);
                    stepsTxt.setText(steps); // Setting the steps text

                    // Load the image using Glide
                    Glide.with(DescriptionActivity.this)
                            .load(imageUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_delete)
                            .into(foodImage);

                    // Clear existing views in the ingredients container
                    ingredientsContainer.removeAllViews();

                    // Initialize previous ingredient view height for positioning
                    int previousViewId = View.generateViewId();
                    for (DataSnapshot ingredientSnapshot : dataSnapshot.child("ingredients").getChildren()) {
                        String ingredientName = ingredientSnapshot.getKey();
                        String ingredientQuantity = ingredientSnapshot.getValue(String.class);

                        // Create a new TextView for each ingredient
                        TextView ingredientTextView = new TextView(DescriptionActivity.this);
                        ingredientTextView.setText(ingredientName + ": " + ingredientQuantity);
                        ingredientTextView.setTextSize(16);

                        // Set the ID for the new TextView
                        ingredientTextView.setId(View.generateViewId());

                        // Add the TextView to the ConstraintLayout
                        ingredientsContainer.addView(ingredientTextView);

                        // Create layout params to set constraints
                        ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                                ConstraintLayout.LayoutParams.WRAP_CONTENT,
                                ConstraintLayout.LayoutParams.WRAP_CONTENT);

                        // Set the constraints for the ingredient TextView
                        params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                        params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;

                        if (previousViewId == View.generateViewId()) {
                            // First ingredient view
                            params.topToBottom = R.id.textView21; // Place under the ingredients label
                        } else {
                            params.topToBottom = previousViewId; // Place under the previous ingredient
                        }

                        ingredientTextView.setLayoutParams(params);

                        // Update previousViewId to the current TextView ID
                        previousViewId = ingredientTextView.getId();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseData", "Error retrieving data: " + databaseError.getMessage());
            }
        });
    }
}
