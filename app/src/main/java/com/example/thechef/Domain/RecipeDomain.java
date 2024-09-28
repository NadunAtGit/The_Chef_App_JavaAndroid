package com.example.thechef.Domain;

import java.io.Serializable;

import java.util.Map;

public class RecipeDomain implements Serializable{
    private String recipeId;
    private String foodName;
    private String description;
    private String imageUrl;
    private String time;
    private Double score;
    private Map<String, String> ingredients; // Store ingredients as key-value pairs

    public RecipeDomain(String recipeId, String foodName, String description, String imageUrl, String time, Double score, Map<String, String> ingredients) {
        this.recipeId = recipeId;
        this.foodName = foodName;
        this.description = description;
        this.imageUrl = imageUrl;
        this.time = time;
        this.score = score;
        this.ingredients = ingredients;
    }

    public String getRecipeId() {
        return recipeId;
    }

    public void setRecipeId(String recipeId) {
        this.recipeId = recipeId;
    }

    // Getters and setters...
    public String getFoodName() {
        return foodName;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTime() {
        return time;
    }

    public Double getScore() {
        return score;
    }

    public Map<String, String> getIngredients() {
        return ingredients;
    }
}

