package com.example.thechef;

import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.thechef.Adapter.MyListAdapter; // Ensure this imports your new adapter
import com.example.thechef.Domain.RecipeDomain;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyRecipes extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MyListAdapter adapter; // Use the new adapter here
    private ArrayList<RecipeDomain> myRecipesList = new ArrayList<>();

    private DatabaseReference recipesRef;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_recipes);

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recycleMyCollection);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Initialize the adapter with an empty list
        adapter = new MyListAdapter(myRecipesList, MyRecipes.this); // Update to new adapter
        recyclerView.setAdapter(adapter); // Set the adapter before fetching data

        // Firebase references
        mAuth = FirebaseAuth.getInstance();
        recipesRef = FirebaseDatabase.getInstance().getReference("recipes");

        // Fetch recipes added by the logged-in user
        fetchMyRecipes();
    }

    private void fetchMyRecipes() {
        String userId = mAuth.getCurrentUser().getUid(); // Get current user's ID
        Log.d("MyRecipes", "Current User ID: " + userId); // Log the user ID

        // Fetch all recipes and filter by userId (assuming each recipe has an addedBy field)
        recipesRef.orderByChild("userId").equalTo(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                        fetchRecipeDetails(recipeSnapshot); // Fetch recipe details for each recipe
                    }
                } else {
                    Toast.makeText(MyRecipes.this, "No recipes found.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MyRecipes.this, "Error fetching recipes.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchRecipeDetails(DataSnapshot recipeSnapshot) {

        String foodName = recipeSnapshot.child("foodName").getValue(String.class);
        String description = recipeSnapshot.child("description").getValue(String.class);
        String recipeId = recipeSnapshot.getKey(); // Get recipe ID from the snapshot key
        String imageUrl = recipeSnapshot.child("imageUrl").getValue(String.class);
        String time = recipeSnapshot.child("time").getValue(String.class);
        Double score = recipeSnapshot.child("score").getValue(Double.class) != null ?
                recipeSnapshot.child("score").getValue(Double.class) : 0.0;
        int ratingCount = recipeSnapshot.child("ratingCount").getValue(Integer.class); // Ensure the field name matches
        String steps = recipeSnapshot.child("steps").getValue(String.class);
        String category = recipeSnapshot.child("category").getValue(String.class);
        String userId = recipeSnapshot.child("addedBy").getValue(String.class); // Assuming addedBy is the userId
        String ingredients = recipeSnapshot.child("ingredients").getValue(String.class);

        // Create RecipeDomain object and add to list
        RecipeDomain recipe = new RecipeDomain(recipeId, foodName, description, imageUrl, time, score, ratingCount, ingredients, steps, category, userId);
        myRecipesList.add(recipe);

        // Update adapter with the recipes
        adapter.notifyItemInserted(myRecipesList.size() - 1); // Notify adapter of the new item added
    }
}
