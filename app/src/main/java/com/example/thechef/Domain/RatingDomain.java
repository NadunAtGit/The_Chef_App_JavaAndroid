package com.example.thechef.Domain;

public class RatingDomain {
    private String recipeId;
    private float score;

    public RatingDomain() {
        //Default constructor required for calls to DataSnapshot.getValue(RatingDomain.class)
    }

    public RatingDomain(String recipeId, float score) {
        this.recipeId = recipeId;
        this.score = score;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }
}
