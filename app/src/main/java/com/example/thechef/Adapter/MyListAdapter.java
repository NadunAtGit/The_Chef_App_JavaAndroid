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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {
    private ArrayList<RecipeDomain> recipeList;
    private Context context;

    public MyListAdapter(ArrayList<RecipeDomain> recipeList, Context context) {
        this.recipeList = recipeList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each recipe in the list
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.my_recipe_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeDomain recipe = recipeList.get(position);

        // Load image using Glide
        Glide.with(context)
                .load(recipe.getImageUrl()) // Load the recipe image
                .into(holder.imageRecipe);

        // Set recipe details
        holder.textRecipeName.setText(recipe.getFoodName());
        holder.textTime.setText(recipe.getTime() + " min");
        holder.textScore.setText(String.format("%.1f", recipe.getScore())); // Display score as text

        // Set click listener to navigate to DescriptionActivity when item is clicked
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DescriptionActivity.class);
            intent.putExtra("recipeId", recipe.getRecipeId()); // Pass the recipe ID to the next activity
            context.startActivity(intent);
        });

        holder.editButton.setOnClickListener(v -> {
            Intent intent = new Intent(context, UpdateActivity.class);
            intent.putExtra("recipeId", recipe.getRecipeId()); // Pass the recipe ID to the UpdateActivity
            context.startActivity(intent);
        });

        holder.deleteButton.setOnClickListener(v -> {
            // Create an AlertDialog for confirmation
            new AlertDialog.Builder(context)
                    .setTitle("Delete Recipe")
                    .setMessage("Are you sure you want to delete this recipe?")
                    .setPositiveButton("Yes", (dialog, which) -> {
                        // Proceed with deletion if user confirms
                        deleteRecipeFromFirebase(recipe.getRecipeId());
                    })
                    .setNegativeButton("No", (dialog, which) -> {
                        // Dismiss the dialog if the user cancels
                        dialog.dismiss();
                    })
                    .show();
        });

    }

    @Override
    public int getItemCount() {
        return recipeList.size(); // Return the number of items in the list
    }

    private void deleteRecipeFromFirebase(String recipeId) {
        // Reference to Firebase Realtime Database
        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);

        // Remove the recipe with the given recipeId
        recipeRef.removeValue().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(context, "Recipe deleted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, "Failed to delete recipe", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageRecipe,editButton,deleteButton; // ImageView for recipe image
        TextView textRecipeName; // TextView for recipe name
        TextView textTime; // TextView for preparation time
        TextView textScore; // TextView for recipe score

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageRecipe = itemView.findViewById(R.id.imageRecipe);
            deleteButton=itemView.findViewById(R.id.deleteButton);
            editButton=itemView.findViewById(R.id.editRecipe);
            textRecipeName = itemView.findViewById(R.id.textRecipeName);
            textTime = itemView.findViewById(R.id.textTime);
            textScore = itemView.findViewById(R.id.textScore); // Ensure this matches the layout
        }
    }
}
