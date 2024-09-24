package com.example.thechef.Activity;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.thechef.Domain.RecipeDomain;
import com.example.thechef.R;

public class DetailActivity extends AppCompatActivity {

    private TextView titleTxt,timeTxt,scoreTxt,descriptionTxt,ingredientsTxt,stepsTxt;
    private ImageView picFood;
    private RecipeDomain object;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initView();
        getBundle();


    }

    private void getBundle() {
        object= (RecipeDomain) getIntent().getSerializableExtra("object");

        int drawableResourceId=this.getResources().getIdentifier(object.getPicurl(),"drawable",this.getPackageName());
        Glide.with(this)
                .load(drawableResourceId)
                .into(picFood);

        titleTxt.setText(object.getTitle());
        timeTxt.setText(object.getTime()+" min");
        scoreTxt.setText(String.valueOf(object.getScore()));
        descriptionTxt.setText(object.getDescription());
        ingredientsTxt.setText(object.getIngredients());
//        stepsTxt.setText(object.getSteps());
    }

    private void initView() {
        titleTxt=findViewById(R.id.titleTxt);
        timeTxt=findViewById(R.id.timeTxt);
        scoreTxt=findViewById(R.id.scoreTxt);
        descriptionTxt=findViewById(R.id.descriptionTxt);
        ingredientsTxt=findViewById(R.id.ingredientsTxt);
        stepsTxt=findViewById(R.id.stepsTxt);
        picFood=findViewById(R.id.picFood);
    }
}