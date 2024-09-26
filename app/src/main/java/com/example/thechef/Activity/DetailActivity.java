package com.example.thechef.Activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.thechef.Domain.RecipeDomain;
import com.example.thechef.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class DetailActivity extends AppCompatActivity {

    private TextView titleTxt, timeTxt, scoreTxt, descriptionTxt, ingredientsTxt, stepsTxt;
    private ImageView picFood;
    private String recipeId; // For Firebase data fetching

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initView(); // Initialize the views
        getBundle(); // Handle the intent data or Firebase fetch
    }

    private void getBundle() {
        // Check if RecipeDomain object is passed via intent
        RecipeDomain object = (RecipeDomain) getIntent().getSerializableExtra("object");

        if (object != null) {
            // Display data from intent
            displayRecipeDetails(object.getFoodName(), object.getDescription(), object.getImageUrl(),
                    object.getTime(), object.getScore(), object.getIngredients());
        } else {
            // If object is null, fetch data from Firebase using recipeId
            recipeId = getIntent().getStringExtra("recipeId");
            if (recipeId != null && !recipeId.isEmpty()) {
                fetchRecipeDataFromFirebase(recipeId);
            } else {
                Log.e("DetailActivity", "No recipe data available from intent or Firebase.");
            }
        }
    }

    // Fetch recipe data from Firebase if no object is passed via intent
    private void fetchRecipeDataFromFirebase(String recipeId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference recipeRef = database.getReference("recipes").child(recipeId);

        recipeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Retrieve recipe data from Firebase
                String foodName = dataSnapshot.child("foodName").getValue(String.class);
                String description = dataSnapshot.child("description").getValue(String.class);
                String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                String time = dataSnapshot.child("time").getValue(String.class);
                Double score = dataSnapshot.child("score").getValue(Double.class);

                // Retrieve ingredients as a Map
                Map<String, String> ingredients = new HashMap<>();
                for (DataSnapshot ingredientSnapshot : dataSnapshot.child("ingredients").getChildren()) {
                    String ingredientName = ingredientSnapshot.getKey();
                    String quantity = ingredientSnapshot.getValue(String.class);
                    ingredients.put(ingredientName, quantity);
                }

                // Display data in the UI
                displayRecipeDetails(foodName, description, imageUrl, time, score, ingredients);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseData", "Error retrieving data: " + databaseError.getMessage());
            }
        });
    }

    // Display recipe details on the UI
    private void displayRecipeDetails(String foodName, String description, String imageUrl, String time, Double score, Map<String, String> ingredients) {
        // Load image using Glide
        Glide.with(this)
                .load(imageUrl)
                .into(picFood);

        // Set data to UI elements
        titleTxt.setText(foodName);
        timeTxt.setText(time + " min");
        scoreTxt.setText(String.valueOf(score));
        descriptionTxt.setText(description);

        // Format ingredients for display
        StringBuilder ingredientsText = new StringBuilder();
        for (Map.Entry<String, String> entry : ingredients.entrySet()) {
            ingredientsText.append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
        }
        ingredientsTxt.setText(ingredientsText.toString());

        // Optionally, set steps or other details
        // stepsTxt.setText(...); // If steps are available
    }

    // Initialize the views from the layout
    private void initView() {
        titleTxt = findViewById(R.id.titleTxt);
        timeTxt = findViewById(R.id.timeTxt);
        scoreTxt = findViewById(R.id.scoreTxt);
        descriptionTxt = findViewById(R.id.descriptionTxt);
        ingredientsTxt = findViewById(R.id.ingredientsTxt);
        stepsTxt = findViewById(R.id.stepsTxt);  // Add steps if needed
        picFood = findViewById(R.id.picFood);
    }
}
