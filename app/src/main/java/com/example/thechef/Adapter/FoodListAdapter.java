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
//this adapter use to show to rating recipes in home page
public class FoodListAdapter extends RecyclerView.Adapter<FoodListAdapter.ViewHolder> {
    //list to store all recipes
    private ArrayList<RecipeDomain> items;
    private Context context;

    //constructor method
    public FoodListAdapter(ArrayList<RecipeDomain> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @NonNull
    @Override
    //by this we get the view for each card from XML layout
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate = LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_food_list, parent, false);
        return new ViewHolder(inflate);
    }

    @Override
    //this binds the data to the view in each card
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecipeDomain currentRecipe = items.get(position);//get the current recipe


        holder.titleTxt.setText(currentRecipe.getFoodName());//set recipe name
        holder.timeTxt.setText(currentRecipe.getTime() + " min");//set cooking time

        //load the selected image to image view
        Glide.with(holder.itemView.getContext())
                .load(currentRecipe.getImageUrl())
                .transform(new GranularRoundedCorners(25, 25, 0, 0))
                .into(holder.pic);

        //reference to the ratings in Firebase
        DatabaseReference ratingsRef = FirebaseDatabase.getInstance().getReference("Ratings");
        //check ratings for this recip
        ratingsRef.child(currentRecipe.getRecipeId()).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //get rating for each user
                float totalScore = 0;
                int count = 0;

                Log.d("RatingData", "DataSnapshot: " + dataSnapshot.toString());


                for (DataSnapshot userRating : dataSnapshot.getChildren()) {

                    Float score = userRating.getValue(Float.class);
                    if (score != null) {
                        totalScore += score;//increase scores
                        count++;
                    }
                }


                if (count > 0) {
                    float averageScore = totalScore / count;
                    holder.scoreTxt.setText(String.format("%.1f", averageScore));//count how many ratings there ar
                } else {
                    holder.scoreTxt.setText("No rating yet");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", databaseError.getMessage());
            }
        });

        //handle item clicks for navigating to description
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
