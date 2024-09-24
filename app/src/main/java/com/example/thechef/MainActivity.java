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

public class MainActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapterFoodList;
    private RecyclerView recyclerViewRecipe;
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

//        initRecyclerView();
        fetchRecipeDataFromFirebase();  // Fetch and log the recipe data from Firebase
    }

//    private void initRecyclerView() {
//        ArrayList<RecipeDomain> items = new ArrayList<>();
//        // Predefined recipes, just for placeholder purposes
//        items.add(new RecipeDomain("Fish Curry", "Cut the fish into large chunks...", "fishcurry", "Steps here..."));
//        items.add(new RecipeDomain("Meat Curry", "", "tunacurry", "", 25, 3.5));
//        items.add(new RecipeDomain("Egg Curry", "", "fast_1", "", 40, 2.0));
//
//        recyclerViewRecipe = findViewById(R.id.view1);
//        recyclerViewRecipe.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
//
//        adapterFoodList = new FoodListAdapter(items);
//        recyclerViewRecipe.setAdapter(adapterFoodList);
//    }

    private void fetchRecipeDataFromFirebase() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference recipeRef = database.getReference("recipes");

        recipeRef.child("R-0001").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Extract individual fields
                    String description = dataSnapshot.child("description").getValue(String.class);
                    String foodName = dataSnapshot.child("foodName").getValue(String.class);
                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);

                    // Log the basic fields
                    Log.d("FirebaseData", "Description: " + description);
                    Log.d("FirebaseData", "Food Name: " + foodName);
                    Log.d("FirebaseData", "Image URL: " + imageUrl);

                    // Extract and log the ingredients dynamically
                    DataSnapshot ingredientsSnapshot = dataSnapshot.child("ingredients");
                    for (DataSnapshot ingredientSnapshot : ingredientsSnapshot.getChildren()) {
                        String ingredientName = ingredientSnapshot.getKey();
                        String quantity = ingredientSnapshot.getValue(String.class);
                        Log.d("FirebaseData", ingredientName + ": " + quantity);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseData", "Error retrieving data: " + databaseError.getMessage());
            }
        });
    }
}
