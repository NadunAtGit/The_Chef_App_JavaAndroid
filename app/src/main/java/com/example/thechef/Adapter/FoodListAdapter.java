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
    private Context context;

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

        // Set title and time
        holder.titleTxt.setText(currentRecipe.getFoodName());
        holder.timeTxt.setText(currentRecipe.getTime() + " min");

        // Load image from URL using Glide
        Glide.with(holder.itemView.getContext())
                .load(currentRecipe.getImageUrl())
                .transform(new GranularRoundedCorners(25, 25, 0, 0))
                .into(holder.pic);

        // Fetch ratings for the current recipe
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("Ratings");
        ratingsRef.child(currentRecipe.getRecipeId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                // Calculate average rating
                float totalScore = 0;
                int count = 0;

                Log.d("RatingData", "DataSnapshot: " + dataSnapshot.toString());

                // Sum all the scores for the recipe
                for (DataSnapshot userRating : dataSnapshot.getChildren()) {
                    // Assuming each child under the recipeId is a user ID with a score as its value
                    Float score = userRating.getValue(Float.class);
                    if (score != null) {
                        totalScore += score; // Accumulate the scores
                        count++; // Count the number of ratings
                    }
                }

                // Calculate average and update scoreTxt
                if (count > 0) {
                    float averageScore = totalScore / count;
                    holder.scoreTxt.setText(String.format("%.1f", averageScore)); // Show average rating
                } else {
                    holder.scoreTxt.setText("0.0"); // Default text if no rating
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", databaseError.getMessage());
            }
        });

        // Handle item clicks for navigating to description
        holder.itemView.setOnClickListener(view -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(holder.itemView.getContext(), DescriptionActivity.class);
                intent.putExtra("recipeId", currentRecipe.getRecipeId());  // Pass recipe ID
                holder.itemView.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTxt, timeTxt, scoreTxt;
        ImageView pic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.titleTxt);
            timeTxt = itemView.findViewById(R.id.timeTxt);
            scoreTxt = itemView.findViewById(R.id.scoreTxt);
            pic = itemView.findViewById(R.id.pic);
        }
    }
}
