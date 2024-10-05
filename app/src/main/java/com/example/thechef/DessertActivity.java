package com.example.thechef;

import android.os.Bundle;
import android.widget.Toast;
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

public class DessertActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SearchResultsAdapter adapter;
    private ArrayList<RecipeDomain> dessertRecipeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dessert);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycleDessert);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch Dessert recipes from Firebase
        fetchDessertRecipes();
    }

    private void fetchDessertRecipes() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference recipeRef = database.getReference("recipes");

        // Fetch only recipes with category "Desserts"
        recipeRef.orderByChild("category").equalTo("Desserts").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dessertRecipeList.clear(); // Clear previous data

                // Iterate through all dessert recipes
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

                    // Ensure category is "Desserts"
                    if ("Desserts".equalsIgnoreCase(category)) {
                        // Add each dessert recipe to the list
                        RecipeDomain dessertRecipe = new RecipeDomain(recipeId, foodName, description, imageUrl, videoUrl, time, score, ratingCount, ingredients, steps, category, userId);
                        dessertRecipeList.add(dessertRecipe);
                    }
                }

                // Set the adapter to display the recipes
                adapter = new SearchResultsAdapter(dessertRecipeList, DessertActivity.this);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(DessertActivity.this, "Error retrieving Dessert recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
