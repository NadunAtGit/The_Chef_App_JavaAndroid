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
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners;
import com.example.thechef.Activity.DetailActivity;
import com.example.thechef.DescriptionActivity;
import com.example.thechef.Domain.RecipeDomain;
import com.example.thechef.R;

import java.util.ArrayList;

public class FoodListAdapter extends RecyclerView.Adapter<FoodListAdapter.ViewHolder> {
ArrayList<RecipeDomain> items;
Context context;

    public FoodListAdapter(ArrayList<RecipeDomain> items) {
        this.items = items;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View inflate= LayoutInflater.from(parent.getContext()).inflate(R.layout.viewholder_food_list,parent,false);
        context=parent.getContext();
        return new ViewHolder(inflate);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // Set title, time, and score
        holder.titleTxt.setText(items.get(position).getFoodName());
        holder.timeTxt.setText(items.get(position).getTime() + " min");
        holder.scoreTxt.setText(String.valueOf(items.get(position).getScore()));

        // Load image from URL using Glide
        String imageUrl = items.get(position).getImageUrl();
        Glide.with(holder.itemView.getContext())
                .load(imageUrl)  // Load from imageUrl instead of drawable
                .transform(new GranularRoundedCorners(25, 25, 0, 0))  // Rounded corners as per original code
                .into(holder.pic);

        // Handle click events
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int currentPosition = holder.getAdapterPosition();

                if (currentPosition != RecyclerView.NO_POSITION) {
                    Intent intent = new Intent(holder.itemView.getContext(), DescriptionActivity.class);

                    // Pass the recipe ID to the new activity
                    intent.putExtra("recipeId", items.get(currentPosition).getRecipeId());

                    holder.itemView.getContext().startActivity(intent);
                }
            }
        });


    }


    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView titleTxt,timeTxt,scoreTxt;
        ImageView pic;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            titleTxt=itemView.findViewById(R.id.titleTxt);
            timeTxt=itemView.findViewById(R.id.timeTxt);
            scoreTxt=itemView.findViewById(R.id.scoreTxt);
            pic=itemView.findViewById(R.id.pic);

        }
    }

}

