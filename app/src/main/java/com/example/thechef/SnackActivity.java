package com.example.thechef;

import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thechef.Adapter.SearchResultsAdapter;
import com.example.thechef.Domain.RecipeDomain;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class SnackActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SearchResultsAdapter adapter;
    private ArrayList<RecipeDomain> snackRecipeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_snack);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycleSnacks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch Snack recipes from Firebase
        fetchSnackRecipes();
    }

    private void fetchSnackRecipes() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference recipeRef = database.getReference("recipes");

        // Fetch only recipes with category "Snacks"
        recipeRef.orderByChild("category").equalTo("Snacks").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                snackRecipeList.clear(); // Clear previous data

                // Iterate through all snack recipes
                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    String recipeId = recipeSnapshot.getKey();
                    String foodName = recipeSnapshot.child("foodName").getValue(String.class);
                    String description = recipeSnapshot.child("description").getValue(String.class);
                    String imageUrl = recipeSnapshot.child("imageUrl").getValue(String.class);
                    String videoUrl = recipeSnapshot.child("videoUrl").getValue(String.class);
                    String time = recipeSnapshot.child("time").getValue(String.class);
                    Double score = recipeSnapshot.child("score").getValue(Double.class);
                    int ratingCount = recipeSnapshot.child("RatingCount").getValue(Integer.class);
                    String ingredients = recipeSnapshot.child("ingredients").getValue(String.class);
                    String steps = recipeSnapshot.child("steps").getValue(String.class);
                    String category = recipeSnapshot.child("category").getValue(String.class);
                    String userId = recipeSnapshot.child("userId").getValue(String.class); // New field for userId

                    // Ensure category is "Snacks"
                    if ("Snacks".equalsIgnoreCase(category)) {
                        // Add each snack recipe to the list
                        RecipeDomain snackRecipe = new RecipeDomain(recipeId, foodName, description, imageUrl, videoUrl, time, score, ratingCount, ingredients, steps, category, userId);
                        snackRecipeList.add(snackRecipe);
                    }
                }

                // Set the adapter to display the recipes
                adapter = new SearchResultsAdapter(snackRecipeList, SnackActivity.this);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SnackActivity.this, "Error retrieving Snack recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
