package com.example.thechef;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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

public class SearchActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private SearchResultsAdapter adapter;
    private ArrayList<RecipeDomain> recipeList = new ArrayList<>();
    private ArrayList<RecipeDomain> filteredList = new ArrayList<>();

    private EditText searchBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchBar = findViewById(R.id.searchbar);
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Fetch all recipes initially
        fetchAllRecipes();

        // Set up a TextWatcher for the search bar
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Filter the recipes based on the search input
                String query = charSequence.toString();
                filterRecipes(query);
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Handle focus change for the search bar
        searchBar.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
                }
            }
        });
    }

    private void fetchAllRecipes() {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference recipeRef = database.getReference("recipes");

        recipeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                recipeList.clear(); // Clear previous results

                for (DataSnapshot recipeSnapshot : dataSnapshot.getChildren()) {
                    String foodName = recipeSnapshot.child("foodName").getValue(String.class);

                    if (foodName != null) {
                        String recipeId = recipeSnapshot.getKey();
                        String description = recipeSnapshot.child("description").getValue(String.class);
                        String imageUrl = recipeSnapshot.child("imageUrl").getValue(String.class);
                        String time = recipeSnapshot.child("time").getValue(String.class);
                        Double score = recipeSnapshot.child("score").getValue(Double.class);
                        int ratingCount = recipeSnapshot.child("RatingCount").getValue(int.class);
                        String steps = recipeSnapshot.child("steps").getValue(String.class);
                        String category = recipeSnapshot.child("category").getValue(String.class);

                        // Retrieve ingredients as a Map (key: ingredient name, value: quantity)
                        Map<String, String> ingredients = new HashMap<>();
                        for (DataSnapshot ingredientSnapshot : recipeSnapshot.child("ingredients").getChildren()) {
                            String ingredientName = ingredientSnapshot.getKey();
                            String quantity = ingredientSnapshot.getValue(String.class);
                            if (ingredientName != null && quantity != null) {
                                ingredients.put(ingredientName, quantity);
                            }
                        }

                        // Add each recipe to the list
                        RecipeDomain recipe = new RecipeDomain(recipeId, foodName, description, imageUrl, time, score, ratingCount, ingredients, steps,category);
                        recipeList.add(recipe);
                    }
                }

                // Initially show all recipes
                adapter = new SearchResultsAdapter(recipeList, SearchActivity.this);
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SearchActivity.this, "Error retrieving data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterRecipes(String query) {
        filteredList.clear(); // Clear previous filtered results

        for (RecipeDomain recipe : recipeList) {
            if (recipe.getFoodName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(recipe);
            }
        }

        // Update the adapter with filtered results
        adapter = new SearchResultsAdapter(filteredList, SearchActivity.this);
        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Clear focus from the search bar when returning to this activity
        searchBar.clearFocus();

        // Optionally, close the keyboard if it's open
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(searchBar.getWindowToken(), 0);
        }
    }
}
