package com.example.thechef.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thechef.Domain.RecipeDomain;
import com.example.thechef.R;
import com.example.thechef.DescriptionActivity;
import com.example.thechef.UpdateActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

//adapter to show recipes in a list
public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {
    private ArrayList<RecipeDomain> recipeList; //list of recipes
    private Context context; //context for managing activity changes

    public MyListAdapter(ArrayList<RecipeDomain> recipeList, Context context) {
        this.recipeList = recipeList; //store the list of recipes
        this.context = context; //store the context
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        //inflate the layout for each recipe card
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_recipe_card, parent, false);
        return new ViewHolder(view); //return the ViewHolder with the layout
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeDomain recipe = recipeList.get(position); //get the current recipe

        // Load the recipe image using Glide
        Glide.with(context)
                .load(recipe.getImageUrl()) //get the image URL from the recipe
                .into(holder.imageRecipe); //set the image to the ImageView

        // Set recipe name and time
        holder.textRecipeName.setText(recipe.getFoodName()); //set the name of the recipe
        holder.textTime.setText(recipe.getTime() + " min"); //set the preparation time

        // Get average score from Firebase database
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("Ratings");
        ratingsRef.child(recipe.getRecipeId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                float totalScore = 0;
                int count = 0;

                //looping all user ratings
                for (DataSnapshot userRating : dataSnapshot.getChildren()) {
                    Float score = userRating.getValue(Float.class); //get each score
                    if (score != null) {
                        totalScore += score; //add the score to the total
                        count++; //increase the count
                    }
                }

                //calculate average score and set it
                if (count > 0) {
                    float averageScore = totalScore / count; //calculate the average score
                    holder.textScore.setText(String.format("%.1f", averageScore)); //display the score
                } else {
                    holder.textScore.setText("No rating yet"); //if no ratings, show this
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                //handle errors while fetching data
                Toast.makeText(context, "Failed to fetch ratings", Toast.LENGTH_SHORT).show();
            }
        });

        //set a click listener for viewing the recipe description
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DescriptionActivity.class);
            intent.putExtra("recipeId", recipe.getRecipeId()); //pass the recipe ID to DescriptionActivity
            context.startActivity(intent); //start the activity
        });

        //set a click listener for editing the recipe
        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, UpdateActivity.class);
            intent.putExtra("recipeId", recipe.getRecipeId()); //pass the recipe ID to UpdateActivity
            context.startActivity(intent); //start the activity
        });

        //set a click listener for deleting the recipe
        holder.deleteButton.setOnClickListener(v -> {
            //show a confirmation dialog before deleting
            new AlertDialog.Builder(context)
                    .setTitle("Delete Recipe") // Set dialog title
                    .setMessage("Are you sure you want to delete this recipe?") //set dialog message
                    .setPositiveButton("Yes", (dialog, which) -> {
                        //call the method to delete the recipe from Firebase
                        deleteRecipeFromFirebase(recipe.getRecipeId());
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        //if 'No' is clicked, just dismiss the dialog
                        dialog.dismiss();
                    })
                    .show(); // Show the dialog
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size(); //return the total number of recipes
    }

    //method to delete a recipe  by its recipe ID
    private void deleteRecipeFromFirebase(String recipeId) {
        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);
        recipeRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Recipe deleted successfully", Toast.LENGTH_SHORT).show(); //show success message
            } else {
                Toast.makeText(context, "Failed to delete recipe", Toast.LENGTH_SHORT).show(); //show error message
            }
        });
    }

    //viewHolder class for managing each recipe item
    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageRecipe, editButton, deleteButton; // UI elements in the recipe card
        TextView textRecipeName, textTime, textScore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageRecipe = itemView.findViewById(R.id.imageRecipe); //image for recipe
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editRecipe); // Button to edit the recipe
            textRecipeName = itemView.findViewById(R.id.textRecipeName); // TextView for recipe name
            textTime = itemView.findViewById(R.id.textTime);
            textScore = itemView.findViewById(R.id.textScore);
        }
    }
}
