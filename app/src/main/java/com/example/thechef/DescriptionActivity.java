package com.example.thechef;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DescriptionActivity extends AppCompatActivity {

    private TextView foodNameTxt, foodDescriptionTxt, stepsTxt;
    private TextView ingredientsContainer;
    private ImageView foodImage,rate;
    private Button save;
    private String recipeId;
    private DatabaseReference savedRecipesRef;
    private FloatingActionButton backButton;
    private FirebaseAuth mAuth; // For user authentication
    private ExoPlayer player; // ExoPlayer instance
    private PlayerView playerView; // PlayerView for video playback

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        foodNameTxt = findViewById(R.id.foodName);
        rate=findViewById(R.id.rate);
        save = findViewById(R.id.save);
        foodDescriptionTxt = findViewById(R.id.foodDescription);
        stepsTxt = findViewById(R.id.Steps);
        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        foodImage = findViewById(R.id.foodImage);
        backButton = findViewById(R.id.backButton);
        playerView = findViewById(R.id.videoView); // Initialize PlayerView
        recipeId = getIntent().getStringExtra("recipeId");

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(DescriptionActivity.this, MainActivity.class));
            }
        });
        rate.setOnClickListener(v -> showRatingDialog(recipeId));
        // Firebase database reference for SavedRecipes
        savedRecipesRef = FirebaseDatabase.getInstance().getReference("SavedRecipes");

        if (recipeId != null) {
            fetchRecipeDataFromFirebase(recipeId);
        }

        // Set click listener for the save button
        save.setOnClickListener(v -> saveRecipe(recipeId));
    }

    private void showRatingDialog(String recipeId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Rate this recipe");

        // Set up the rating bar
        final RatingBar ratingBar = new RatingBar(this);
        ratingBar.setRating(0); // Default rating

        builder.setView(ratingBar);

        // Set up the dialog buttons
        builder.setPositiveButton("Submit", (dialog, which) -> {
            float rating = roundToNearestHalf(ratingBar.getRating());
            submitRating(recipeId, rating);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private float roundToNearestHalf(float rating) {
        return Math.round(rating * 2) / 2.0f; // Round to nearest 0.5
    }

    // Submit rating to Firebase
    private void submitRating(String recipeId, float rating) {
        DatabaseReference ratingRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);

        ratingRef.child("ratings").child(mAuth.getCurrentUser().getUid()).setValue(rating)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(DescriptionActivity.this, "Rating submitted!", Toast.LENGTH_SHORT).show();
                    updateAverageRating(recipeId, rating);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(DescriptionActivity.this, "Failed to submit rating.", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateAverageRating(String recipeId, float newRating) {
        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);

        recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Float currentScore = snapshot.child("score").getValue(Float.class);
                    Long ratingCount = snapshot.child("ratingCount").getValue(Long.class);

                    if (currentScore == null) currentScore = 0f;
                    if (ratingCount == null) ratingCount = 0L;

                    // Calculate new average score
                    float newAverage = ((currentScore * ratingCount) + newRating) / (ratingCount + 1);
                    newAverage = roundToNearestHalf(newAverage); // Round to nearest 0.5

                    // Update score and increment rating count
                    recipeRef.child("score").setValue(newAverage);
                    recipeRef.child("ratingCount").setValue(ratingCount + 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FirebaseError", "Error updating score: " + error.getMessage());
            }
        });
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
                    String videoUrl = dataSnapshot.child("videoUrl").getValue(String.class); // Get video URL
                    String ingredients = dataSnapshot.child("ingredients").getValue(String.class); // Retrieve ingredients as is

                    // Set the text in the TextViews
                    foodNameTxt.setText(foodName);
                    foodDescriptionTxt.setText(description);
                    stepsTxt.setText(steps);
                    ingredientsContainer.setText(ingredients);

                    // Load the image using Glide
                    Glide.with(DescriptionActivity.this)
                            .load(imageUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_delete)
                            .into(foodImage);

                    // Validate and play the video if the URL is valid
                    if (videoUrl != null && !videoUrl.isEmpty()) {
                        initializePlayer(videoUrl);
                    } else {
                        // Show a message if video URL is null or empty
                        Toast.makeText(DescriptionActivity.this, "Video not available for this recipe.", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseData", "Error retrieving data: " + databaseError.getMessage());
            }
        });
    }


    private void initializePlayer(String videoUrl) {
        player = new ExoPlayer.Builder(this).build();
        playerView.setPlayer(player);

        // Prepare the media item
        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play(); // Start playing
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release(); // Release the player when not in use
            player = null;
        }
    }

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
