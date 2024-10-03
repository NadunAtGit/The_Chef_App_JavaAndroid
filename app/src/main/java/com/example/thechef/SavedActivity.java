package com.example.thechef;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.thechef.Adapter.SearchResultsAdapter;
import com.example.thechef.Domain.RecipeDomain;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SavedActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SearchResultsAdapter adapter;
    private ArrayList<RecipeDomain> savedRecipesList = new ArrayList<>();

    private DatabaseReference savedRecipesRef;
    private DatabaseReference recipesRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_saved);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Firebase references
        mAuth = FirebaseAuth.getInstance();
        savedRecipesRef = FirebaseDatabase.getInstance().getReference("SavedRecipes");
        recipesRef = FirebaseDatabase.getInstance().getReference("recipes");

        // Fetch saved recipes for the logged-in user
        fetchSavedRecipes();
    }

    private void fetchSavedRecipes() {
        String userId = mAuth.getCurrentUser().getUid(); // Get current user's ID

        // Fetch saved recipe IDs from Firebase for the current user
        savedRecipesRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                        String recipeId = recipeSnapshot.getKey(); // Get recipe ID
                        fetchRecipeDetails(recipeId); // Fetch recipe details for each saved recipe ID
                    }
                } else {
                    Toast.makeText(SavedActivity.this, "No saved recipes found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SavedActivity.this, "Error fetching saved recipes.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRecipeDetails(String recipeId) {
        recipesRef.child(recipeId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot recipeSnapshot) {
                if (recipeSnapshot.exists()) {
                    String foodName = recipeSnapshot.child("foodName").getValue(String.class);
                    String description = recipeSnapshot.child("description").getValue(String.class);
                    String imageUrl = recipeSnapshot.child("imageUrl").getValue(String.class);
                    String time = recipeSnapshot.child("time").getValue(String.class);
                    Double score = recipeSnapshot.child("score").getValue(Double.class);
                    int ratingCount = recipeSnapshot.child("RatingCount").getValue(Integer.class);
                    String steps = recipeSnapshot.child("steps").getValue(String.class);
                    String category = recipeSnapshot.child("category").getValue(String.class);
                    String ingredients = recipeSnapshot.child("ingredients").getValue(String.class);
                    String userId = recipeSnapshot.child("userId").getValue(String.class);

                    // Concatenate ingredients into a single string


                    // Create RecipeDomain object with the concatenated ingredients
                    RecipeDomain recipe = new RecipeDomain(recipeId, foodName, description, imageUrl, time, score, ratingCount, ingredients, steps, category,userId);
                    savedRecipesList.add(recipe);

                    // Update adapter with the saved recipes
                    adapter = new SearchResultsAdapter(savedRecipesList, SavedActivity.this);
                    recyclerView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SavedActivity.this, "Error fetching recipe details.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
