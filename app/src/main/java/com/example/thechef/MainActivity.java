package com.example.thechef;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thechef.Adapter.FoodListAdapter;
import com.example.thechef.Domain.RecipeDomain;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    private RecyclerView.Adapter adapterFoodList;
    private ConstraintLayout pizza,mains,salads,soups,drinks,desserts,snacks,curry;
    private RecyclerView recyclerViewRecipe;
    private ArrayList<RecipeDomain> items = new ArrayList<>();  // Store recipes here
    ImageView profile, addrecipe, saved, myRecipes;
    EditText searchbar;
    private TextView welcomeText;
    private String currentUserId;

    // Firebase authentication instance
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Get current user
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserId = currentUser.getUid();
        } else {
            // Handle the case when the user is not logged in (optional)
            Log.e("MainActivity", "User not logged in.");
            return; // Exit the onCreate if no user is logged in
        }

        // Initialize views
        profile = findViewById(R.id.profile);
        soups=findViewById(R.id.soups);
        drinks=findViewById(R.id.drinks);
        desserts=findViewById(R.id.desserts);
        snacks=findViewById(R.id.snacks);
        curry=findViewById(R.id.curry);
        pizza = findViewById(R.id.PizzaButton);
        salads=findViewById(R.id.salads);
        mains=findViewById(R.id.mains);
        myRecipes = findViewById(R.id.myRecipes);
        saved = findViewById(R.id.saved);
        addrecipe = findViewById(R.id.addrecipe);
        searchbar = findViewById(R.id.searchbar);
        welcomeText = findViewById(R.id.welcomeText);

        // Set up search bar focus change listener
        searchbar.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                startActivity(new Intent(MainActivity.this, SearchActivity.class));
                searchbar.clearFocus();
            }
        });

        // OnClickListeners for various buttons
        addrecipe.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, AddRecipe.class)));
        pizza.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, PizzaActivity.class)));
        mains.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MainCourseActivity.class)));
        soups.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SoupActivity.class)));
        curry.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, CurryActivity.class)));
        salads.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SaladsActivity.class)));
        snacks.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SnackActivity.class)));
        desserts.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DessertActivity.class)));
        drinks.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, DrinksActivity.class)));
        myRecipes.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, MyRecipes.class)));
        saved.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, SavedActivity.class)));
        profile.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, profileActivity.class)));

        initRecyclerView();  // Initialize RecyclerView
        fetchRecipeDataFromFirebase();  // Fetch data from Firebase
        fetchCurrentUserData(); // Fetch current user's name
    }

    // Method to fetch current user's name
    private void fetchCurrentUserData() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            // Check if the user signed in with Google (displayName available)
            String displayName = currentUser.getDisplayName();

            if (displayName != null && !displayName.isEmpty()) {
                // If the display name is available (Google sign-in), use it
                String[] nameParts = displayName.split(" ");
                String firstName = nameParts[0]; // Get the first part of the name

                welcomeText.setText("Hello " + firstName); // Set welcome text for Google user
            } else {
                // If the user signed in via email, fetch the name from Firebase Realtime Database
                fetchEmailUserName();
            }
        } else {
            // Handle case when no user is logged in (optional)
            welcomeText.setText("Hello Guest");
            Log.e("MainActivity", "User not logged in.");
        }
    }

    // Method to fetch the user's name from Firebase if signed in with email
    private void fetchEmailUserName() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("Users").child(currentUserId); // Adjust path if needed

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class); // Get the user's name
                    if (name != null) {
                        welcomeText.setText("Hello " + name); // Set welcome text for email user
                    } else {
                        welcomeText.setText("Hello User"); // Fallback if name is null
                    }
                } else {
                    Log.e("FirebaseData", "User does not exist in database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseData", "Error retrieving user data: " + databaseError.getMessage());
            }
        });
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
                    String videoUrl = recipeSnapshot.child("videoUrl").getValue(String.class);
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
                    RecipeDomain recipe = new RecipeDomain(recipeId, foodName, description, imageUrl, videoUrl, time, score, ratingCount, ingredients, steps, category, userId);
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
