package com.example.thechef;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thechef.Adapter.FoodListAdapter;
import com.example.thechef.Domain.RecipeDomain;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapterFoodList;
    private RecyclerView recyclerViewRecipe;
    private ArrayList<RecipeDomain> items = new ArrayList<>();  // Store recipes here
    ImageView profile, addrecipe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profile = findViewById(R.id.profile);
        addrecipe = findViewById(R.id.addrecipe);

        addrecipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, AddRecipe.class));
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, profileActivity.class));
            }
        });

        initRecyclerView();  // Initialize RecyclerView
        fetchRecipeDataFromFirebase();  // Fetch data from Firebase
    }

    // Initialize RecyclerView without predefined items
    private void initRecyclerView() {
        recyclerViewRecipe = findViewById(R.id.view1);
        recyclerViewRecipe.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        // Initially pass an empty list to the adapter
        adapterFoodList = new FoodListAdapter(items,this);
        recyclerViewRecipe.setAdapter(adapterFoodList);
    }

    private void fetchRecipeDataFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference recipeRef = database.getReference("recipes");

        recipeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                items.clear();  // Clear previous items

                // Iterate over all recipes in Firebase
                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    String recipeId = recipeSnapshot.getKey();  // Get the recipe ID (document key)
                    String foodName = recipeSnapshot.child("foodName").getValue(String.class);
                    String description = recipeSnapshot.child("description").getValue(String.class);
                    String imageUrl = recipeSnapshot.child("imageUrl").getValue(String.class);
                    String time = recipeSnapshot.child("time").getValue(String.class);
                    Double score = recipeSnapshot.child("score").getValue(Double.class);
                    int RatingCount = recipeSnapshot.child("RatingCount").getValue(int.class);
                    String steps = recipeSnapshot.child("steps").getValue(String.class); // Fetching the steps

                    // Retrieve ingredients as a Map (key: ingredient name, value: quantity)
                    Map<String, String> ingredients = new HashMap<>();
                    for (DataSnapshot ingredientSnapshot : recipeSnapshot.child("ingredients").getChildren()) {
                        String ingredientName = ingredientSnapshot.getKey();
                        String quantity = ingredientSnapshot.getValue(String.class);
                        if (ingredientName != null && quantity != null) {  // Check for null values
                            ingredients.put(ingredientName, quantity);
                        }
                    }

                    // Add each recipe to the items list with recipeId
                    RecipeDomain recipe = new RecipeDomain(recipeId, foodName, description, imageUrl, time, score,RatingCount, ingredients, steps);
                    items.add(recipe);
                }

                // Notify the adapter that the data has changed so it can refresh the view
                adapterFoodList.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseData", "Error retrieving data: " + databaseError.getMessage());
            }
        });
    }
}
