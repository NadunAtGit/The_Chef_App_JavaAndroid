package com.example.thechef;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
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
    private ConstraintLayout pizza;
    private RecyclerView recyclerViewRecipe;
    private ArrayList<RecipeDomain> items = new ArrayList<>();  // Store recipes here
    ImageView profile, addrecipe, saved, myRecipes;
    EditText searchbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        profile = findViewById(R.id.profile);
        pizza = findViewById(R.id.PizzaButton);
        myRecipes = findViewById(R.id.myRecipes);
        saved = findViewById(R.id.saved);
        addrecipe = findViewById(R.id.addrecipe);
        searchbar = findViewById(R.id.searchbar);

        searchbar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                // Start SearchActivity when the search bar gains focus
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                // Optionally, you can clear the focus to avoid repeated triggering
                searchbar.clearFocus();
            }
        });


        // OnClickListeners for various buttons
        addrecipe.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddRecipe.class)));
        pizza.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PizzaActivity.class)));
        myRecipes.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MyRecipes.class)));
        saved.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SavedActivity.class)));
        profile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, profileActivity.class)));

        initRecyclerView();  // Initialize RecyclerView
        fetchRecipeDataFromFirebase();  // Fetch data from Firebase
    }

    // Initialize RecyclerView without predefined items
    private void initRecyclerView() {
        recyclerViewRecipe = findViewById(R.id.view1);
        recyclerViewRecipe.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapterFoodList = new FoodListAdapter(items, this);
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
                    int ratingCount = recipeSnapshot.child("RatingCount").getValue(Integer.class);
                    String steps = recipeSnapshot.child("steps").getValue(String.class); // Fetching the steps
                    String category = recipeSnapshot.child("category").getValue(String.class);
                    String userId = recipeSnapshot.child("userId").getValue(String.class); // Fetching user ID

                    // Fetching ingredients as a single string
                    StringBuilder ingredientsBuilder = new StringBuilder();
                    for (DataSnapshot ingredientSnapshot : recipeSnapshot.child("ingredients").getChildren()) {
                        String ingredientName = ingredientSnapshot.getKey();
                        String quantity = ingredientSnapshot.getValue(String.class);
                        if (ingredientName != null && quantity != null) {  // Check for null values
                            ingredientsBuilder.append(ingredientName).append(": ").append(quantity).append("\n");
                        }
                    }
                    String ingredients = ingredientsBuilder.toString().trim(); // Convert to String and trim

                    // Add each recipe to the items list with recipeId
                    RecipeDomain recipe = new RecipeDomain(recipeId, foodName, description, imageUrl, time, score, ratingCount, ingredients, steps, category, userId);
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

    @Override
    protected void onResume() {
        super.onResume();
        // Clear focus from the search bar to prevent cursor from blinking
        searchbar.clearFocus();
    }
}
