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

    private TextView foodNameTxt, foodDescriptionTxt, stepsTxt, scoreTxt;
    private TextView ingredientsContainer;
    private ImageView foodImage, rate;
//    private Button save;
    private ConstraintLayout share;
    private String recipeId;
    private DatabaseReference savedRecipesRef;
    private DatabaseReference ratingsRef;
    private FloatingActionButton backButton,save;
    private FirebaseAuth mAuth; // For user authentication
    private ExoPlayer player; // ExoPlayer instance
    private PlayerView playerView; // PlayerView for video playback
    private RatingBar ratingBar; // RatingBar for recipe rating

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_description);

        // Initialize FirebaseAuth instance
        mAuth = FirebaseAuth.getInstance();

        // Initialize views
        foodNameTxt = findViewById(R.id.foodName);
        scoreTxt = findViewById(R.id.scoreTxt);
        rate = findViewById(R.id.rate);
        share = findViewById(R.id.shareButton);
        save = findViewById(R.id.saveButton);
        foodDescriptionTxt = findViewById(R.id.foodDescription);
        stepsTxt = findViewById(R.id.Steps);
        ingredientsContainer = findViewById(R.id.ingredientsContainer);
        foodImage = findViewById(R.id.foodImage);
        backButton = findViewById(R.id.backButton);
        playerView = findViewById(R.id.videoView); // Initialize PlayerView
        ratingBar = findViewById(R.id.ratingBar); // Initialize RatingBar

        // Firebase database reference
        savedRecipesRef = FirebaseDatabase.getInstance().getReference("SavedRecipes");
        ratingsRef = FirebaseDatabase.getInstance().getReference("Ratings");

        recipeId = getIntent().getStringExtra("recipeId");

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(DescriptionActivity.this, MainActivity.class));
        });

        share.setOnClickListener(v -> shareRecipe());

        if (recipeId != null) {
            fetchRecipeDataFromFirebase(recipeId);
            fetchUserRating(recipeId);  // Fetch existing rating for the current user
            fetchAverageRating(recipeId);  // Fetch average rating for this recipe
        }

        // Set click listener for the save button
        save.setOnClickListener(v -> saveRecipe(recipeId));

        // Listen for rating changes and save rating
        ratingBar.setOnRatingBarChangeListener((ratingBar, rating, fromUser) -> {
            if (fromUser) {
                saveRating(recipeId, rating);
            }
        });
    }

    // Sharing the recipe
    private void shareRecipe() {
        String shareText = "Check out this recipe: " + foodNameTxt.getText().toString() +
                "\n\n" + foodDescriptionTxt.getText().toString() +
                "\n\nIngredients:\n" + ingredientsContainer.getText().toString() +
                "\n\nSteps:\n" + stepsTxt.getText().toString();

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain"); // Sharing plain text
        shareIntent.setPackage("com.whatsapp"); // Share via WhatsApp

        // Add the text to share
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);

        // Launch WhatsApp or show a message if not installed
        try {
            startActivity(shareIntent);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(DescriptionActivity.this, "WhatsApp is not installed on your device.", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchRecipeDataFromFirebase(String recipeId) {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference recipeRef = database.getReference("recipes").child(recipeId);

        recipeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String foodName = dataSnapshot.child("foodName").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                    String steps = dataSnapshot.child("steps").getValue(String.class);
                    String videoUrl = dataSnapshot.child("videoUrl").getValue(String.class);
                    String ingredients = dataSnapshot.child("ingredients").getValue(String.class);

                    foodNameTxt.setText(foodName);
                    foodDescriptionTxt.setText(description);
                    stepsTxt.setText(steps);
                    ingredientsContainer.setText(ingredients);

                    Glide.with(DescriptionActivity.this)
                            .load(imageUrl)
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .error(android.R.drawable.ic_delete)
                            .into(foodImage);

                    if (videoUrl != null && !videoUrl.isEmpty()) {
                        initializePlayer(videoUrl);
                    } else {
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

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoUrl));
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (player != null) {
            player.release();
            player = null;
        }
    }

    // Save recipe method
    private void saveRecipe(String recipeId) {
        String userId = mAuth.getCurrentUser().getUid(); // Get current user's ID
        savedRecipesRef.child(userId).child(recipeId).setValue(true).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(DescriptionActivity.this, "Recipe saved successfully!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(DescriptionActivity.this, "Failed to save recipe.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Save rating method
    private void saveRating(String recipeId, float rating) {
        String userId = mAuth.getCurrentUser().getUid();
        ratingsRef.child(recipeId).child(userId).setValue(rating).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(DescriptionActivity.this, "Rating saved!", Toast.LENGTH_SHORT).show();
                ratingBar.setIsIndicator(true); // Lock the RatingBar after rating
                fetchAverageRating(recipeId);  // Refresh the average rating after saving a new rating
            } else {
                Toast.makeText(DescriptionActivity.this, "Failed to save rating.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Fetch user rating
    private void fetchUserRating(String recipeId) {
        String userId = mAuth.getCurrentUser().getUid();
        ratingsRef.child(recipeId).child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Float userRating = snapshot.getValue(Float.class);
                    if (userRating != null) {
                        ratingBar.setRating(userRating); // Set the user's previous rating
                        ratingBar.setIsIndicator(true); // Lock the RatingBar if the user has rated
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FetchRating", "Error fetching rating: " + error.getMessage());
            }
        });
    }

    // Fetch average rating for the recipe
    private void fetchAverageRating(String recipeId) {
        ratingsRef.child(recipeId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                float totalRating = 0;
                int ratingCount = 0;

                for (DataSnapshot ratingSnapshot : snapshot.getChildren()) {
                    Float rating = ratingSnapshot.getValue(Float.class);
                    if (rating != null) {
                        totalRating += rating;
                        ratingCount++;
                    }
                }

                if (ratingCount > 0) {
                    float averageRating = totalRating / ratingCount;
                    scoreTxt.setText(String.valueOf(averageRating));
                } else {
                    scoreTxt.setText("0.0");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("AverageRating", "Error fetching average rating: " + error.getMessage());
            }
        });
    }
}
