package com.example.thechef.Adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

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
        //inflate the item layout for each recipe in the search results
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_recipe_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeDomain recipe = recipeList.get(position);

        //load image using Glide
        Glide.with(context)
                .load(recipe.getImageUrl()) //load the recipe image
                .into(holder.imageRecipe);

        //set recipe details
        holder.textRecipeName.setText(recipe.getFoodName());
        holder.textTime.setText(recipe.getTime() + " min");

        //fetch and display the average score from the Ratings table
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("Ratings");
        ratingsRef.child(recipe.getRecipeId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Calculate average rating
                float totalScore = 0;
                int count = 0;

                Log.d("RatingData", "DataSnapshot: " + dataSnapshot.toString());

                // Sum all the scores for the recipe
                for (DataSnapshot userRating : dataSnapshot.getChildren()) {
                    //assuming each child under the recipeId is a user ID with a score as its value
                    Float score = userRating.getValue(Float.class);
                    if (score != null) {
                        totalScore += score; //accumulate the scores
                        count++; //count the number of ratings
                    }
                }

                //calculate average and update textDescription
                if (count > 0) {
                    float averageScore = totalScore / count;
                    holder.textDescription.setText(String.format("%.1f", averageScore)); //show average rating
                } else {
                    holder.textDescription.setText("No rating yet"); //default text if no rating
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", databaseError.getMessage());
            }
        });

        //set onclick listener to navigate to DescriptionActivity when item is clicked
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DescriptionActivity.class);
            intent.putExtra("recipeId", recipe.getRecipeId()); //pass the recipe ID to the next activity
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size(); //return the number of items in the list
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageRecipe;
        TextView textRecipeName;
        TextView textDescription;
        TextView textTime; //textView for preparation time

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageRecipe = itemView.findViewById(R.id.imageRecipe);
            textRecipeName = itemView.findViewById(R.id.textRecipeName);
            textDescription = itemView.findViewById(R.id.textScore); //ensure this ID matches your layout
            textTime = itemView.findViewById(R.id.textTime);
        }
    }
}
