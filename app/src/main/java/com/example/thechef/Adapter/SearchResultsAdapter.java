package com.example.thechef.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide; // Ensure you have Glide for image loading
import com.example.thechef.Domain.RecipeDomain;
import com.example.thechef.R;
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
        // Inflate the new card layout for search results
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeDomain recipe = recipeList.get(position);

        // Load image using Glide
        Glide.with(context)
                .load(recipe.getImageUrl()) // Assuming your RecipeDomain has getImageUrl() method
                .into(holder.imageRecipe);

        holder.textRecipeName.setText(recipe.getFoodName());
        holder.textDescription.setText(String.valueOf(recipe.getScore()));
        holder.textDescription.setText(String.valueOf(recipe.getScore()));
        holder.textTime.setText(recipe.getTime()+"min");
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageRecipe;
        TextView textRecipeName;
        TextView textDescription;
        TextView textTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageRecipe = itemView.findViewById(R.id.imageRecipe);
            textRecipeName = itemView.findViewById(R.id.textRecipeName);
            textDescription = itemView.findViewById(R.id.textDescription);
            textTime = itemView.findViewById(R.id.textTime);
        }
    }
}
