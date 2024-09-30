package com.example.thechef.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.example.thechef.DescriptionActivity;
import com.example.thechef.Domain.RecipeDomain;
import com.example.thechef.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class FoodListAdapter extends RecyclerView.Adapter<FoodListAdapter.ViewHolder> {
    private ArrayList<RecipeDomain> items;
    private Context context;  // Initialize context

    public FoodListAdapter(ArrayList<RecipeDomain> items, Context context) {
        this.items = items;
        this.context = context;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_food_list, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeDomain currentRecipe = items.get(position);

        // Set title, time, and score
        holder.titleTxt.setText(currentRecipe.getFoodName());
        holder.timeTxt.setText(currentRecipe.getTime() + " min");
        holder.scoreTxt.setText(String.valueOf(currentRecipe.getScore()));

        // Load image from URL using Glide
        Glide.with(holder.itemView.getContext())
                .load(currentRecipe.getImageUrl())
                .transform(new GranularRoundedCorners(25, 25, 0, 0))
                .into(holder.pic);

        // Handle item clicks for navigating to description
        holder.itemView.setOnClickListener(view -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(holder.itemView.getContext(), DescriptionActivity.class);
                intent.putExtra("recipeId", currentRecipe.getRecipeId());  // Pass recipe ID
                holder.itemView.getContext().startActivity(intent);
            }
        });

        // Handle rating click
        holder.rate.setOnClickListener(v -> showRatingDialog(currentRecipe.getRecipeId()));  // Pass recipe ID to dialog
    }

    // Show rating dialog
    private void showRatingDialog(String recipeId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);  // Use initialized context
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_rating, null);

        builder.setView(dialogView);

        // Initialize RatingBar and Button
        RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
        Button submitButton = dialogView.findViewById(R.id.button_submit);

        AlertDialog dialog = builder.create();

        // Submit button click listener
        submitButton.setOnClickListener(v -> {
            float newRating = ratingBar.getRating();
            updateScoreInFirebase(recipeId, newRating);
            dialog.dismiss();
        });

        dialog.show();  // Display dialog
    }

    // Update the recipe's score in Firebase
    private void updateScoreInFirebase(String recipeId, float newRating) {
        DatabaseReference recipeRef = FirebaseDatabase.getInstance().getReference("recipes").child(recipeId);

        // Retrieve current score and rating count
        recipeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Float currentScore = snapshot.child("score").getValue(Float.class);
                    Long ratingCount = snapshot.child("ratingCount").getValue(Long.class);

                    // Ensure values are not null
                    if (currentScore == null) currentScore = 0f;
                    if (ratingCount == null) ratingCount = 0L;

                    // Calculate new average score
                    float newAverage = ((currentScore * ratingCount) + newRating) / (ratingCount + 1);

                    // Update score and increment rating count
                    recipeRef.child("score").setValue(newAverage);
                    recipeRef.child("ratingCount").setValue(ratingCount + 1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle error
                Log.e("FirebaseError", "Error updating score: " + error.getMessage());
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt, timeTxt, scoreTxt;
        ImageView pic, rate;  // Reference for rate ImageView

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
            scoreTxt = itemView.findViewById(R.id.scoreTxt);
            pic = itemView.findViewById(R.id.pic);
            rate = itemView.findViewById(R.id.rate);  // Initialize rate ImageView
        }
    }
}
