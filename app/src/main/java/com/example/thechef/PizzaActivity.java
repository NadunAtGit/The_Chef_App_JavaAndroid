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
import java.util.HashMap;
import java.util.Map;

public class PizzaActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SearchResultsAdapter adapter;
    private ArrayList<RecipeDomain> pizzaRecipeList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pizza);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclepizza);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch Pizza recipes from Firebase
        fetchPizzaRecipes();
    }

    private void fetchPizzaRecipes() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference recipeRef = database.getReference("recipes");

        // Fetch only recipes with category "Pizza"
        recipeRef.orderByChild("category").equalTo("Pizza").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                pizzaRecipeList.clear(); // Clear previous data

                // Iterate through all pizza recipes
                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    String recipeId = recipeSnapshot.getKey();
                    String foodName = recipeSnapshot.child("foodName").getValue(String.class);
                    String description = recipeSnapshot.child("description").getValue(String.class);
                    String imageUrl = recipeSnapshot.child("imageUrl").getValue(String.class);
                    String time = recipeSnapshot.child("time").getValue(String.class);
                    Double score = recipeSnapshot.child("score").getValue(Double.class);
                    int ratingCount = recipeSnapshot.child("RatingCount").getValue(int.class);
                    String steps = recipeSnapshot.child("steps").getValue(String.class);
                    String category = recipeSnapshot.child("category").getValue(String.class);

                    Map<String, String> ingredients = new HashMap<>();
                    for (DataSnapshot ingredientSnapshot : recipeSnapshot.child("ingredients").getChildren()) {
                        String ingredientName = ingredientSnapshot.getKey();
                        String quantity = ingredientSnapshot.getValue(String.class);
                        if (ingredientName != null && quantity != null) {
                            ingredients.put(ingredientName, quantity);
                        }
                    }

                    // Ensure category is "Pizza"
                    if ("Pizza".equalsIgnoreCase(category)) {
                        // Add each pizza recipe to the list
                        RecipeDomain pizzaRecipe = new RecipeDomain(recipeId, foodName, description, imageUrl, time, score, ratingCount, ingredients, steps, category);
                        pizzaRecipeList.add(pizzaRecipe);
                    }
                }

                // Set the adapter to display the recipes
                adapter = new SearchResultsAdapter(pizzaRecipeList, PizzaActivity.this);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PizzaActivity.this, "Error retrieving Pizza recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
