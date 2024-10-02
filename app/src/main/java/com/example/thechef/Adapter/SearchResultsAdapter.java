package com.example.thechef.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.thechef.Domain.RecipeDomain;
import com.example.thechef.R;
import com.example.thechef.DescriptionActivity;

import java.util.ArrayList;

public class SearchResultsAdapter extends RecyclerView.Adapter<SearchResultsAdapter.ViewHolder> {
    private ArrayList<RecipeDomain> recipeList;
    private Context context;

    public SearchResultsAdapter(ArrayList<RecipeDomain> recipeList, Context context) {
        this.recipeList = recipeList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each recipe in the search results
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_card, parent, false);
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
        holder.textDescription.setText(String.valueOf(recipe.getScore()));
        holder.textTime.setText(recipe.getTime() + " min");

        // Set click listener to navigate to DescriptionActivity when item is clicked
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DescriptionActivity.class);
            intent.putExtra("recipeId", recipe.getRecipeId()); // Pass the recipe ID to the next activity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size(); // Return the number of items in the list
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageRecipe; // ImageView for recipe image
        TextView textRecipeName; // TextView for recipe name
        TextView textDescription; // TextView for recipe score
        TextView textTime; // TextView for preparation time

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageRecipe = itemView.findViewById(R.id.imageRecipe);
            textRecipeName = itemView.findViewById(R.id.textRecipeName);
            textDescription = itemView.findViewById(R.id.textDescription);
            textTime = itemView.findViewById(R.id.textTime);
        }
    }
}
